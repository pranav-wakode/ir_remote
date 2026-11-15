#include <Arduino.h>

// CRITICAL FOR ESP32-S3: Define the send pin BEFORE including the library
#define IR_SEND_PIN 5 
#include <IRremote.hpp>

const uint8_t IR_RECEIVE_PIN = 4;

void setup() {
  // Use 115200 baud for stable USB-C OTG communication
  Serial.begin(115200);
  delay(1000);

  IrReceiver.begin(IR_RECEIVE_PIN, DISABLE_LED_FEEDBACK);
  IrSender.begin(); 

  Serial.println("READY");
}

void loop() {
  // -------------------------------------------------------------
  // 1. LISTEN FOR COMMANDS FROM ANDROID (TRANSMITTING)
  // -------------------------------------------------------------
  if (Serial.available() > 0) {
    String command = Serial.readStringUntil('\n');
    command.trim(); // Clean up hidden carriage returns

    // Command Format: SEND:NEC:9088:44
    if (command.startsWith("SEND:NEC:")) {
      int lastColon = command.lastIndexOf(':');
      String addrHex = command.substring(9, lastColon);
      String cmdHex = command.substring(lastColon + 1);

      // Convert Hex strings to integers
      uint16_t address = strtoul(addrHex.c_str(), NULL, 16);
      uint8_t cmd = strtoul(cmdHex.c_str(), NULL, 16);

      IrSender.sendNEC(address, cmd, 3); // 3 repeats for reliability
      Serial.println("ACK:SENT:NEC");
    }
    
    // Command Format: SEND:RAW:8000,4000,500,1500...
    else if (command.startsWith("SEND:RAW:")) {
      String arrayStr = command.substring(9);
      uint16_t rawData[250]; // Buffer large enough for complex AC remotes
      int count = 0;
      int startIdx = 0;
      int commaIdx = arrayStr.indexOf(',');

      // Parse the comma-separated string into the uint16_t array
      while (commaIdx != -1 && count < 250) {
        rawData[count++] = arrayStr.substring(startIdx, commaIdx).toInt();
        startIdx = commaIdx + 1;
        commaIdx = arrayStr.indexOf(',', startIdx);
      }
      // Grab the final value after the last comma
      if (startIdx < arrayStr.length() && count < 250) {
        rawData[count++] = arrayStr.substring(startIdx).toInt();
      }

      // Blast the raw microsecond timings at standard 38kHz
      IrSender.sendRaw(rawData, count, 38);
      Serial.println("ACK:SENT:RAW");
    }
  }

  // -------------------------------------------------------------
  // 2. LISTEN FOR PHYSICAL REMOTES (LEARNING MODE)
  // -------------------------------------------------------------
  if (IrReceiver.decode()) {
    if (IrReceiver.decodedIRData.protocol == NEC) {
      // Output clean format: RECV:NEC:9088:44
      Serial.print("RECV:NEC:");
      Serial.print(IrReceiver.decodedIRData.address, HEX);
      Serial.print(":");
      Serial.println(IrReceiver.decodedIRData.command, HEX);
    } else {
      // For Unknown protocols, use the built-in compensation function.
      // This fixes the TSOP receiver stretch and outputs a perfect clone array.
      Serial.println("RECV:RAW_ARRAY:");
      IrReceiver.compensateAndPrintIRResultAsCArray(&Serial, false);
    }
    
    IrReceiver.resume();
  }
}
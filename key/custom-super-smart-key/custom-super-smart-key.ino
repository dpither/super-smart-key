#include <NimBLEDevice.h>

class ServerCallbacks : public NimBLEServerCallbacks {
  void onDisconnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo, int reason) override {
    Serial.printf("Client disconnected - start advertising\n");
    NimBLEDevice::startAdvertising();
  }
} serverCallbacks;

void setup() {
  Serial.begin(115200);
  Serial.println("Starting NimBLE Server\n");

  NimBLEDevice::init("Super Smart Key");

  NimBLEServer* pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(&serverCallbacks);

  NimBLEAdvertising* pAdvertising = NimBLEDevice::getAdvertising();
  // pAdvertising->setScanResponse(true);
  // BLEAdvertisementData scanData;
  // scanData.setName("ESP32_Test");
  // pAdvertising->setScanResponseData(scanData);
  pAdvertising->setMinInterval(0x20);
  pAdvertising->setMaxInterval(0x40);
  BLEAdvertisementData advData;
  advData.setFlags(0x06);  // General discoverable, BR/EDR not supported
  pAdvertising->setAdvertisementData(advData);
  pAdvertising->setName("Super Smart Key");
  pAdvertising->start();
}

void loop() {
  delay(2000);
}
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
  pAdvertising->setName("Super Smart Key");
  pAdvertising->start();
}

void loop() {
  delay(2000);
}
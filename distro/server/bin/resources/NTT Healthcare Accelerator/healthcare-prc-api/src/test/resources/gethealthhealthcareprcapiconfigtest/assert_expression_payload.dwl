%dw 2.0
import * from dw::test::Asserts
---
payload must equalTo({
  "correlationId": "49b6a6b0-cd78-11ed-a2cc-6c02e07cbe2b",
  "transactionId": "b7518883779e4c469b5390fa63454dcc",
  "message": "Healthcheck for healthcare-prc-api is successfull"
})
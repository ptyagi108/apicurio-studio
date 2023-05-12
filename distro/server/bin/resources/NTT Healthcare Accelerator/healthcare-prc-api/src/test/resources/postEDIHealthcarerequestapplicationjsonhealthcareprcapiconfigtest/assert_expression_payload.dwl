%dw 2.0
import * from dw::test::Asserts
---
payload must equalTo({
  "271": "ISA*00*          *00*          *ZZ*1234567        *ZZ*11111          *170508*1141*>*00501*000000101*1*P*:~\nGS*HC*XXXXXXX*XXXXX*20170617*1741*101*X*005010X279A1~\nST*271*4321*005010X279A1~\nBHT*0022*11*10001234*20060501*1319~\nHL*1**20*1~\nNM1*PR*2*ABC COMPANY*****PI*842610001~\nHL*2*1*21*1~\nNM1*1P*2*BONE AND JOINT CLINIC*****SV*2000035~\nHL*3*2*22*0~\nTRN*2*93175-012547*9877281234~\nNM1*IL*1*SMITH*JOHN****MI*123456789~\nN3*15197 BROADWAY AVENUE*APT 215~\nN4*KANSAS CITY*MO*64108~\nDMG*D8*19630519*M~\nDTP*346*D8*20060101~\nEB*1**30**GOLD 123 PLAN~\nEB*L~\nEB*1**1>33>35>47>86>88>98>AL>MH>UC~\nEB*B**1>33>35>47>86>88>98>AL>MH>UC*HM*GOLD 123 PLAN*27*10*****Y~\nEB*B**1>33>35>47>86>88>98>AL>MH>UC*HM*GOLD 123 PLAN*27*30*****N~\nLS*2120~\nNM1*P3*1*JONES*MARCUS****SV*0202034~\nLE*2120~\nSE*22*4321~\nGE*1*101~\nIEA*1*000000101~",
  "271-json": {
    "Errors": [
      {
        "interchangeId": 101,
        "errorType": "INTERCHANGE_NOTE",
        "groupId": -1,
        "errorCode": "025",
        "transactionId": "",
        "errorLevel": "INTERCHANGE_LEVEL",
        "fatal": true,
        "errorText": "Duplicate Interchange Control Number",
        "segment": 1,
        "segTag": "",
        "errorDetails": "Duplicate interchange control number 101"
      }
    ],
    "TransactionSets": {},
    "FunctionalAcksGenerated": [],
    "InterchangeAcksGenerated": [
      {
        "Interchange": {
          "ISA09": "2023-03-28",
          "ISA04": "",
          "ISA15": "P",
          "ISA03": "00",
          "ISA14": "1",
          "ISA02": "",
          "ISA13": 101,
          "ISA01": "00",
          "ISA12": "00501",
          "ISA08": "1234567",
          "ISA07": "ZZ",
          "ISA06": "11111",
          "ISA05": "ZZ",
          "ISA16": ":",
          "ISA11": ">",
          "ISA10": 70740000
        },
        "TA104": "R",
        "TA105": "025",
        "TA102": "2023-03-28",
        "TA103": 70740000,
        "TA101": 101
      }
    ]
  }
})
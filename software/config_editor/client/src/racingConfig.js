export default {
  "version": "0.1",
  "settings": {
    "port": 38911
  },
  "triggers": [
    {
      "condition": {
        "operator": "And",
        "conditions": [
          {
            "property": "ButtonId",
            "operator": "==",
            "value": 11
          },
          {
            "property": "EventType",
            "operator": "In",
            "values": [
              "Init",
              "ButtonDown",
              "ButtonUp",
              "PotentiometerState"
            ]
          }
        ]
      },
      "actions": [
        {
          "action": "ForwardMessage",
          "hostName": "192.168.29.192",
          "port": 38911
        }
      ]
    }
  ]
};
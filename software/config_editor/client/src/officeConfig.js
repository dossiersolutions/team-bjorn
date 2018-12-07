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
          "action": "LogEvent"
        },
        {
          "action": "Triggers",
          "triggers": [
            {
              "condition": {
                "property": "EventType",
                "operator": "And",
		"conditions": [
		  {
                    "property": "EventType",
                    "operator": "In",
                    "values": [
                      "ButtonDown"
                    ]
                  },
                  {
                    "property": "QuantizedPotentiometerState",
                    "operator": "==",
                    "value": 0
                  }
		]
              },
              "actions": [
                {
                  "action": "Shell",
                  "command": "curl http://192.168.29.183:3000/start"
                }
              ]
            },
            {
              "condition": {
                "property": "EventType",
                "operator": "And",
		"conditions": [
		  {
                    "property": "EventType",
                    "operator": "In",
                    "values": [
                      "ButtonDown"
                    ]
                  },
                  {
                    "property": "QuantizedPotentiometerState",
                    "operator": "==",
                    "value": 1
                  }
		]
              },
              "actions": [
                {
                  "action": "Shell",
                  "command": "sh pi@192.168.29.9 'aplay getupstandup.wav'"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
};
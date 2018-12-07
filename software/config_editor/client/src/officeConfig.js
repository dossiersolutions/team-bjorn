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
        // {
        //   "action": "LogEvent"
        // },
        {
          "action": "Triggers",
          "triggers": ["grenade.wav", "hyperspace.wav", "pipe.wav", "screetch_pop.wav", "teleporter.wav"].map((file, i) => ({
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
                  "property": "PotentiometerStep",
                  "operator": "==",
                  "value": i + 1
                }
              ]
            },
            "actions": [
              {
                "action": "Shell",
                "command": "ssh pi@192.168.29.9 'aplay " + file + "'"
              }
            ]
          })).concat([{
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
                  "property": "PotentiometerStep",
                  "operator": "==",
                  "value": 0
                }
              ]
            },
            "actions": [
              {
                "action": "Shell",
                "command": "curl 192.168.29.183:3000/start"
              }
            ]
          }])
        }
      ]
    }
  ]
};
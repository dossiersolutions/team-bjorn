import React, {useState, useEffect} from 'react';
import './App.css';
import Root from './Root';
import {Button} from "@blueprintjs/core"
import officeConfig from "./officeConfig";
import racingConfig from "./racingConfig";

const endpoint = "http://192.168.29.205:4567/config";

function App() {

  const [config, setConfig] = useState(null);

  useEffect(
    () => {
      fetch(endpoint)
        .then(response => response.json())
        .then(setConfig)
    },
    () => false
  )

  const pushConfig = (config) => {
      fetch(endpoint, {
        method: "UPDATE",
        body: JSON.stringify(config)
      })
      .then(setConfig(config))
  };
  
  console.log(config);

  return (
    <div className="App">
      <h1>ThatButton ConfigEditor</h1>

      <h2>Presets:</h2>
      <Button
        icon="play"
        text={"DosseRacing 2018"}
        onClick={() => {
          pushConfig(racingConfig);
        }}
      />
      <Button
        icon="office"
        text={"Awesome Office Automation"}
        onClick={() => {
          pushConfig(officeConfig);
        }}
      />

      <h3>Editor:</h3>
      {
        !config && <>
          <p>Fetching config from server...</p>
        </>
      }

      {
        config && <Root config={config} setConfig={setConfig}/>
      }

    </div>
  );
}

export default App;

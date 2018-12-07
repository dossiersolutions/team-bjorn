import React, {useState, useEffect} from 'react';
import './App.css';
import Root from './Root';

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
  
  console.log(config);

  return (
    <div className="App">
      <h1>ThatButton ConfigEditor</h1>

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

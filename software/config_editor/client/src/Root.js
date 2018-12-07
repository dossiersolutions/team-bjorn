import React from 'react';
import List from './List';
import Trigger from './Trigger';


function Root({config, setConfig}) {
  return <>
    <List
      data={config.triggers}
      component={Trigger}
      setData={(triggers) => setConfig({version: 0.1, triggers})}
    />
  </>;
}

export default Root;
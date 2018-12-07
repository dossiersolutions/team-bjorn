import React from 'react';
import List from './List';
import Condition from './Condition';
import Action from './Action';

function Trigger({ data, setData }) {
  if (!setData) throw new Error("missing setter!");

  return <>
    <h4>Trigger</h4>
    <div style={{ paddingLeft: "1em" }}>
      <Condition
        data={data.condition}
        setData={condition => setData({...data, condition})}
      />

      <List
        data={data.actions}
        setData={actions => setData({...data, actions})}
        component={Action}
      />
    </div>
  </>;
}

Trigger.template = {
  conditions: [],
  actions: []
}

export default Trigger;
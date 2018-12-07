import React from 'react';
// import Condition from './Condition';
import List from './List';
import { Tab, Tabs } from "@blueprintjs/core"

function AndCondition({data, setData}) {
  if (!setData) throw new Error("missing setter!");

  return <>
    <h4>Condition And</h4>
    <List
      data={data.conditions}
      setData={conditions => setData({...data, conditions})}
      component={Condition}
      template={{operator: "And", conditions: []}}
    />
  </>;
}

function Comparison({data, setData}) {
  return <h4>Condition {data.property} {data.operator} {data.value}</h4>;
}

function Condition({ data, setData }) {
  return <>
    <h4>Condition</h4>
    <Tabs id="Condition" onChange={value => setData({condition: value})} selectedTabId={data.operator}>
      <Tab id="And" title="And"/>
      <Tab id="==" title="=="/>
    </Tabs>

    {
      (data.operator === "And") && <AndCondition data={data} setData={setData}/>
    }
    {
      (data.operator === "==") && <Comparison data={data} setData={setData}/>
    }
  </>;
}

Condition.template = {
  condition: "And",
  conditions: []
}

export default Condition;
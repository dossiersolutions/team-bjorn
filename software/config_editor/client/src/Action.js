import React from 'react';
import { Tab, Tabs } from "@blueprintjs/core"

function Action({ data, setData }) {
  if (!setData) throw new Error("missing setter!");
  return <>
    {/* <h4>Action</h4> */}
    <div>
      <Tabs id="Action" onChange={value => setData({condition: value})} selectedTabId={data.action}>
        <Tab id="LogEvent" title="LogEvent"/>
        <Tab id="Shell" title="Shell"/>
      </Tabs>
    </div>

    {/* {
      (data.action === "LogEvent") && <p>No parameters</p>
    } */}

    {
      (data.action === "Shell") && <p>{data.command}</p>
    }
  </>;
}

Action.template = {
  action: "LogEvent"
};

export default Action;


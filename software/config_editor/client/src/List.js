import React from 'react';
import {Button} from "@blueprintjs/core"


function List({data, setData, component}) {
  if (!setData) throw new Error("missing setter!");

  const Component = component;
  return <>
    <div style={{ paddingLeft: "1em" }}>
      {
        data.map((item, index) => {
          return <Component
            key={index}
            data={item}
            setData={newItem => {
              const newItems = [...data];
              newItems[index] = newItem;
              setData(newItems);
            }}
          />;
        })
      }
      <Button
        icon="add"
        text={component.name }
        onClick={() => {
          setData([...data, component.template])
        }}
      />
      <p>
        </p>
    </div>
  </>;
}

export default List;
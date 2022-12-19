export default (key, name, type) => {
  if (!type) type = "camunda";
  const TYPE_TARGET = {
    activiti: "http://activiti.org/bpmn",
    camunda: "http://bpmn.io/schema/bpmn",
    flowable: "http://flowable.org/bpmn"
  };
  return  `<?xml version="1.0" encoding="UTF-8"?>
  <bpmn2:definitions 
  targetNamespace="http://www.activiti.org/processdef"
   expressionLanguage="http://www.w3.org/1999/XPath" 
   typeLanguage="http://www.w3.org/2001/XMLSchema"
    xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
     xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" 
     xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
     xmlns:activiti="http://activiti.org/bpmn" 
     xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
    xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd"
    id="diagram_${key}"
    targetNamespace="${TYPE_TARGET[type]}">
    <bpmn2:process id="${key}" name="${name}" isExecutable="true">
    </bpmn2:process>
    <bpmndi:BPMNDiagram id="BPMNDiagram_1">
      <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
      </bpmndi:BPMNPlane>
    </bpmndi:BPMNDiagram>
  </bpmn2:definitions>` ;
/*
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions 
targetNamespace="http://www.activiti.org/processdef"
 expressionLanguage="http://www.w3.org/1999/XPath" 
 typeLanguage="http://www.w3.org/2001/XMLSchema"
  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
   xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" 
   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
   xmlns:activiti="http://activiti.org/bpmn" 
   xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd"
  id="diagram_${key}"
  targetNamespace="${TYPE_TARGET[type]}">
  <bpmn2:process id="${key}" name="${name}" isExecutable="true">
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${key}">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>*/
}
  

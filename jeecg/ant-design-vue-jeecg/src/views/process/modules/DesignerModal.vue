<!--
 * @Author: niaier-pc niaier@outlook.com
 * @Date: 2022-12-15 12:56:09
 * @LastEditors: niaier-pc niaier@outlook.com
 * @LastEditTime: 2022-12-17 21:20:29
 * @FilePath: \ant-design-vue-jeecg\src\views\process\modules\DesignerModal.vue
 * @Description: 
 * 
 * Copyright (c) 2022 by niaier-pc niaier@outlook.com, All Rights Reserved. 
-->
<!--
 * @Author: niaier-pc niaier@outlook.com
 * @Date: 2022-12-15 12:56:09
 * @LastEditors: niaier-pc niaier@outlook.com
 * @LastEditTime: 2022-12-16 22:33:46
 * @FilePath: \ant-design-vue-jeecg\src\views\process\modules\DesignerModal.vue
 * @Description: 
 * 
 * Copyright (c) 2022 by niaier-pc niaier@outlook.com, All Rights Reserved. 
-->
<!-- 组件说明 -->
<template>
  <a-drawer :visible="visible" width="100%" @close="onClose">
    <div class="action">
      <a-button-group>
        <a href="javascript:" @click="$refs.refFile.click()"><a-button>加载本地BPMN文件</a-button></a>
        <input type="file" id="files" ref="refFile" style="display: none" @change="loadXML" />
        <a href="javascript:" ref="saveXML" title="保存为bpmn">
          <a-button>保存为BPMN文件</a-button>
        </a>
        <a href="javascript:" ref="saveSvg" title="保存为svg">
          <a-button>保存为SVG图片</a-button>
        </a>
        <a href="javascript:" class="active" @click="handlerUndo" title="撤销操作"><a-button>撤销</a-button></a>

        <a href="javascript:" class="active" @click="handlerRedo" title="恢复操作"><a-button>恢复</a-button></a>
      </a-button-group>
    </div>
    <div class="view">
      <a-button-group>
        <a-button></a-button>
        <a-button></a-button>
        <a-button></a-button>
        <a-button></a-button>
        <a-button></a-button>
        <a-button></a-button>
      </a-button-group>
    </div>
    <div class="containers">
      <div class="canvas" ref="canvas"></div>
    </div>


    <div class="footer">



    </div>
  </a-drawer>

</template>

<script>
//import x from ''
import BpmnModeler from "bpmn-js/lib/Modeler"
import { xmlStr } from "@/mock/xmlStr"
export default {
  name: 'DesignerModal',
  components: {

  },
  data () {
    return {
      visible: false,
      bpmnModeler: null,
      container: null,
      canvas: null,
      xmlStr: xmlStr
    };
  },
  computed: {

  },
  mounted () {
  },
  methods: {
    add () {
      this.visible = true
      console.log('object');
      this.$nextTick(() => {
        this.init()

      })
    },
    onClose () {
      this.visible = false
    },
    init () {
      const canvas = this.$refs.canvas;
      this.bpmnModeler = new BpmnModeler({
        container: canvas
      });

      this.createNewDiagram();
    },
    async createNewDiagram () {
      try {
        const result = await this.bpmnModeler.importXML(this.xmlStr);
        const { warnings } = result;
        console.log(warnings);

        this.success();
      } catch (err) {
        console.log(err.message, err.warnings);
      }
    },
    success () {
      this.addBpmnListener();
    },
    async loadXML () {
      const that = this;
      const file = this.$refs.refFile.files[0];

      var reader = new FileReader();
      reader.readAsText(file);
      reader.onload = function () {
        that.xmlStr = this.result;
        that.createNewDiagram();
      };
    },
    // 监听变化
    async addBpmnListener () {
      const that = this;
      const downloadLink = this.$refs.saveXML;
      const downloadSvgLink = this.$refs.saveSvg;

      async function opscoffee () {
        try {
          const result = await that.saveSVG();
          const { svg } = result;

          that.setEncoded(downloadSvgLink, "bpmn.svg", svg);
        } catch (err) {
          console.log(err);
        }

        try {
          const result = await that.saveXML();
          const { xml } = result;

          that.setEncoded(downloadLink, "bpmn.xml", xml);
        } catch (err) {
          console.log(err);
        }
      }

      opscoffee();
      this.bpmnModeler.on("commandStack.changed", opscoffee);
    },
    async saveSVG (done) {
      try {
        const result = await this.bpmnModeler.saveSVG(done);
        return result;
      } catch (err) {
        console.log(err);
      }
    },
    async saveXML (done) {
      try {
        const result = await this.bpmnModeler.saveXML({ format: true }, done);
        return result;
      } catch (err) {
        console.log(err);
      }
    },
    setEncoded (link, name, data) {
      const encodedData = encodeURIComponent(data);
      if (data) {
        link.href = "data:application/bpmn20-xml;charset=UTF-8," + encodedData;
        link.download = name;
      }
    },
    handlerRedo () {
      this.bpmnModeler.get("commandStack").redo();
    },
    handlerUndo () {
      this.bpmnModeler.get("commandStack").undo();
    }


  },
}
</script>

<style scoped>
.containers {
  width: 100%;
  height: calc(100vh - 82px);
}

.canvas {
  width: 100%;
  height: 100%;
}
</style>
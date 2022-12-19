<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-form-model-item ref="title" label="标题" prop="title">
      <a-input
        placeholder="标题"
        v-model="form.title"
        @blur="
          () => {
            $refs.title.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-row :gutter="0">
      <a-col :md="12" :lg="12" style="width:380px;margin-left:90px">
        <a-form-model-item
          ref="publish_user"
          label="发布人："
          prop="publish_user"
        >
          <a-input
            placeholder="发布人"
            v-model="form.publish_user"
            style="margin-left:15px;"
            @blur="
              () => {
                $refs.publish_user.onFieldBlur();
              }
            "
          />
        </a-form-model-item>
      </a-col>
      <a-col :md="12" :lg="12">
        <a-form-model-item
          ref="publish_time"
          label="发布时间"
          prop="publish_time"
        >
         <a-input
            placeholder="发布时间"
            v-model="form.publish_time"
            style="margin-left:15px;"
            @blur="
              () => {
                $refs.publish_time.onFieldBlur();
              }
            "
          />
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-form-model-item
      v-if="form.type != '1'"
      ref="live_address"
      label="图片地址"
      prop="live_address"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-upload
        v-model:fileList="fileList"
        name="file"
        list-type="picture-card"
        class="avatar-uploader"
        :show-upload-list="false"
        :customRequest="uploadImage"
        :before-upload="beforeUpload"
        @change="handleChange"
      >
        <img v-if="imageUrl" width="100px" :src="imageUrl" alt="avatar" />
        <div v-else>
          <!-- todo -->
          <a-icon :type="loading ? 'loading' : 'plus'" />
          <div class="ant-upload-text">选择</div>
        </div>
      </a-upload>
    </a-form-model-item>
    <a-form-model-item label="排序" prop="order_by">
      <a-input-number
        placeholder="排序"
        id="inputNumber"
        v-model="form.order_by"
        :min="0"
        :max="1000"
        @change=""
      />
    </a-form-model-item>
    <a-form-model-item :label="introTitle" prop="intro">
      <a-input v-model="form.intro" :placeholder="introTitle" type="textarea" />
    </a-form-model-item>
    <a-form-model-item label="内容" prop="intro" v-if="form.type != '1'">
      <rich-text
        class="richText"
        :text="form.content"
        @editorChange="editorChange"
      />
    </a-form-model-item>
    <a-form-model-item label="备注" prop="remark">
      <a-input v-model="form.remark" placeholder="备注" type="textarea" />
    </a-form-model-item>
  </a-form-model>
</template>
<script>
import { saveOrUpdate } from "@/api/common/graphic";
import RichText from "@/components/Common/RichText";
import { upload } from "@/api/common/upload";
function getBase64(img, callback) {
  const reader = new FileReader();
  reader.addEventListener("load", () => callback(reader.result));
  reader.readAsDataURL(img);
}
export default {
  // 声明当前子组件接收父组件传递的属性
  props: {
    record: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      labelCol: { span: 4 },
      wrapperCol: { span: 18 },
      other: "",
      mybol: false,
      form: {
        id: "",
        type: "",
        title: "",
        intro: "",
        content: "",
        tpdz: "",
        publish_user: "",
        publish_time: "",
        read_num: "",
        status: "",
        order_by: 0,
        remark: "",
      },
      imageUrl: "",
      fileList: [],
      loading: false,
      introTitle: "简介",
      rules: {
        title: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        publish_user: [
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        publish_time: [
          { min: 0, max: 120, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        intro: [
          {
            min: 0,
            max: 500,
            message: "长度不得大于500个字符",
            trigger: "blur",
          },
        ],
        remark: [
          {
            min: 0,
            max: 500,
            message: "长度不得大于500个字符",
            trigger: "blur",
          },
        ],
      },
    };
  },
  components: { RichText },
  mounted() {
    this.form = this.record;
    this.imageUrl = this.record.show_tpdz;
    if (this.form.type == "1") {
      this.introTitle = "内容";
    }
  },
  methods: {
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    editorChange: function (html) {
      this.form.content = html;
    },
    onOk() {
      console.log("监听了 modal ok 事件");
      return new Promise((resolve) => {
        this.$refs.ruleForm.validate((valid) => {
          if (valid) {
            saveOrUpdate(this.form).then((response) => {
              resolve(true);
            });
            return true;
          } else {
            console.log("error submit!!");
            return false;
          }
        });
      });
    },
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    handleChange(info) {
      if (info.file.status === "uploading") {
        this.loading = true;
        return;
      }
      if (info.file.status === "done") {
        // Get this url from response in real world.
        getBase64(info.file.originFileObj, (imageUrl) => {
          this.imageUrl = imageUrl;
          this.loading = false;
        });
      }
    },
    beforeUpload(file) {
      const isJpgOrPng =
        file.type === "image/jpeg" || file.type === "image/png";
      if (!isJpgOrPng) {
        this.$message.error("You can only upload JPG file!");
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error("Image must smaller than 2MB!");
      }
      return isJpgOrPng && isLt2M;
    },
    // 上传头像
    uploadImage(file) {
      this.loading = true;
      const formData = new FormData();
      formData.append("file", file.file);
      upload(formData).then(
        (res) => {
          if (res.status == "200") {
            this.loading = false;
            this.imageUrl = res.data.data.show_file_path;
            this.form.tpdz = res.data.data.file_path;
            this.form.fileIds = res.data.data.id;
          }
        },
        (err) => {
          this.avatarLoading = false;
        }
      );
    },
  },
};
</script>
<style scoped>
.richText {
  overflow-y: auto;
  height: 240px;
}
.ql-container {
  height: 320px !important;
}
</style>
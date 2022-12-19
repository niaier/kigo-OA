import React, { useState ,useEffect ,useImperativeHandle} from 'react';
import { Tree ,message } from 'antd';
const { DirectoryTree } = Tree;

import { findList } from '../service';


export type TreeProps = {
  onSelect: (values: any) => Promise<void>;
  myFef:any
};

const MyTree: React.FC<TreeProps> = (props) => {
    const [treeData, setTreeData] = useState<any>([]);
    const [expandedKeys, setExpandedKeys] = useState<any>([]);
    const [autoExpandParent, setAutoExpandParent] = useState<boolean>(true);

  const fommat =  (arrayList:any,pidStr = "${tablePd.tree_pid}",idStr = "id",childrenStr = "children") => {
    arrayList.push({'name':'${tablePd.table_comment}','id':'1','${tablePd.tree_pid}':'0'})
    let listOjb = {}; // 用来储存{key: obj}格式的对象
    let treeList = []; // 用来储存最终树形结构数据的数组
    // 将数据变换成{key: obj}格式，方便下面处理数据
    for (let i = 0; i < arrayList.length; i++) {
      var data = arrayList[i];
      data.title = data.name;
      data.key = data.id;
      data.value = data.id;
      if (data.child_num == 0) {
        data.isLeaf = true;
      }
      listOjb[arrayList[i][idStr]] = data;
    }
    // 根据pid来将数据进行格式化
    for (let j = 0; j < arrayList.length; j++) {
      // 判断父级是否存在
      let haveParent = listOjb[arrayList[j][pidStr]];
      if (haveParent) {
        // 如果有没有父级children字段，就创建一个children字段
        !haveParent[childrenStr] && (haveParent[childrenStr] = []);
        // 在父级里插入子项
        haveParent[childrenStr].push(arrayList[j]);
      } else {
        // 如果没有父级直接插入到最外层
        treeList.push(arrayList[j]);
      }
    }
    return treeList;
  };
  
  const findMyList = async () => {
    const hide = message.loading('正在查询');
    try {
      await findList({}).then(res =>{
        const treeData = fommat(
          res.data,
          "${tablePd.tree_pid}"
        );
        let expandedKeys = [];
        expandedKeys.push('1');
        setTreeData(treeData);
        expandedKeys.push(treeData[0].children[0].id);
        setExpandedKeys(expandedKeys);
        props.onSelect(treeData[0].children[0]);
      })
      hide();
      // message.success('数据查询成功');
      return true;
    } catch (error) {
      hide();
      // message.error('数据查询失败');
      return false;
    }
  };

  useEffect(() => {
    findMyList();
  }, []);

  // 暴露的子组件方法，给父组件调用
  useImperativeHandle(props.myFef,() => {
    return {
       _childFn() {
        findMyList();
       }
   }
  })

  const onSelect = (keys: React.Key,info: any) => {
    // console.log('Trigger Select', keys ,info);
    props.onSelect(info.node);
  };

  const onExpand = (expandedKeysValue: React.Key[]) => {
    // console.log('Trigger Expand');
    setExpandedKeys(expandedKeysValue);
    setAutoExpandParent(false);
  };

  return (
    <DirectoryTree
      // multiple
      defaultExpandAll
      onExpand={onExpand}
      expandedKeys={expandedKeys}
      autoExpandParent={autoExpandParent}
      onSelect={onSelect}
      treeData={treeData}
    />
  );
};

export default MyTree;

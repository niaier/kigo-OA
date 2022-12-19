var CustomizeOrganizeSelectMorePopupCtrl = ['$scope','$http','$rootScope', function($scope,$http,$rootScope) {

    $scope.name="";
    $scope.short_name="";
    $scope.clearData = function (){
        $scope.name="";
        $scope.short_name="";
        findOrganize($rootScope.organize_parent_id);
    }

    $scope.searchOrganize = function (){
        $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findOrganizeList?name="+$scope.name+"&short_name="+$scope.short_name).success(function(response){
            $scope.orgData=response.data;
        });
    }


    $scope.save = function() {
        if($scope.organize_ids!=undefined&&$scope.organize_ids!=""){
            var idx = $scope.organize_ids.indexOf("");
            if( idx != -1){//不存在就添加
                $scope.organize_ids.splice(idx,1);
                $scope.organize_names.splice(idx,1);
            }
            $scope.saveMoreOrganize($scope.organize_ids,$scope.organize_names);
        }else{
            $scope.saveMoreOrganize("","");
        }
        $scope.$hide();
    };

    $scope.close = function() {
        $scope.$hide();
    };

    initData();
    function initData() {
        if($scope.treedata==undefined){
            $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findOrganizeList").success(function(response){
                $scope.treedata=fommat(response.data);
                $scope.expandedNodes = [
                    $scope.treedata[0],
                    $scope.treedata[0].children[0]
                ];
                if($scope.treedata[0].children[0]!=undefined&&$scope.treedata[0].children[0]!=""){
                    findOrganize($scope.treedata[0].children[0].id);
                }
            });
        }else{
            $scope.expandedNodes = [$scope.treedata[0],
                $scope.treedata[0].children[0]
            ];
        }

        $scope.buttonClick = function($event, node) {
            $scope.node = node;
        }
        $scope.showSelected = function(sel) {
            $scope.selectedNode = sel;
            findOrganize(sel.id);
        };
        $scope.opts = {
            // dirSelectable: false
        }
        initSelectData();
    }

    function findOrganize(node_id){
        $rootScope.organize_parent_id=node_id;
        $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findOrganizeList?parent_id="+node_id+"&name="+$scope.name+"&short_name="+$scope.short_name).success(function(response){
            $scope.orgData=response.data;
        });
    }

    if($rootScope.organize_parent_id!=undefined&&$rootScope.organize_parent_id!=null&&$rootScope.organize_parent_id!=''){
        findOrganize($rootScope.organize_parent_id)
    }

    $scope.selectOrg = function(data,event) {
        var action = event.target;
        var organize_ids = [];
        var organize_names = [];
        if($scope.organize_ids!=undefined){
            organize_ids = $scope.organize_ids.toString().split(',');
            organize_names = $scope.organize_names.toString().split(',');
        }
        if(action.checked){//选中，就添加
            if(organize_ids.indexOf(data.id) == -1){//不存在就添加
                organize_ids.push(data.id);
                $scope.organize_ids = organize_ids;
                organize_names.push(data.name)
                $scope.organize_names = organize_names;
            }
        }else{//去除就删除result里
            var idx = organize_ids.indexOf(data.id);
            if( idx != -1){//不存在就添加
                $scope.organize_ids.splice(idx,1);
                $scope.organize_names.splice(idx,1);
            }
        }
        initSelectData();
    }


    $scope.delOrg = function(data) {
        var idx = $scope.organize_ids.indexOf(data.organize_id);
        if( idx != -1){//不存在就添加
            $scope.organize_ids.splice(idx,1);
            $scope.organize_names.splice(idx,1);
        }
        initSelectData();
    }

    function initSelectData(){
        if($scope.organize_ids!=undefined){
            $scope.selectData = [];
            for (let i = 0; i < $scope.organize_ids.length; i++) {
                if($scope.organize_ids[i]!=""){
                    $scope.selectData.push({"organize_id":$scope.organize_ids[i],"organize_name":$scope.organize_names[i]})
                }
            }
        }
    }

    //被选中条件：ng-checked的结果为true
    $scope.isSelected = function(id){
        if($scope.organize_ids!=undefined){
            var organize_ids = $scope.organize_ids.toString();
            return organize_ids.indexOf(id)!=-1;
        }else{
            return false;
        }
    };


    function fommat(arrayList,pidStr = "parent_id",idStr = "id",childrenStr = "children") {
        arrayList.push({'name':'组织信息','id':'1','parent_id':'0','org_cascade':'org_1_','level_num':'0'})
        let listOjb = {}; // 用来储存{key: obj}格式的对象
        let treeList = []; // 用来储存最终树形结构数据的数组
        // 将数据变换成{key: obj}格式，方便下面处理数据
        for (let i = 0; i < arrayList.length; i++) {
            var data = arrayList[i];
            data.title = data.name;
            data.key = data.id;
            data.value = data.org_cascade + ":" + data.level_num;
            if (data.child_num == 0) {
                data.isLeaf = true;
                data[childrenStr]=[];
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

}];
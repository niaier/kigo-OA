
var CustomizeUserSelectMorePopupCtrl = ['$scope','$http','$rootScope', function($scope,$http,$rootScope) {

    $scope.login_name="";
    $scope.name="";
    $scope.phone="";
    $scope.clearData = function (){
        $scope.name="";
        $scope.login_name="";
        $scope.phone="";
        findUser($rootScope.organize_parent_id);
    }

    $scope.searchUser = function (){
        $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findUserList?login_name="+$scope.login_name+"&name="+$scope.name+"&phone="+$scope.phone).success(function(response){
            $scope.userData=response.data;
        });
    }


    $scope.save = function() {
        if($scope.user_ids!=undefined&&$scope.user_ids!=""){
            var idx = $scope.user_ids.indexOf("");
            if( idx != -1){//不存在就添加
                $scope.user_ids.splice(idx,1);
                $scope.user_names.splice(idx,1);
            }
            $scope.saveMoreUser($scope.user_ids,$scope.user_names);
        }else{
            $scope.saveMoreUser("","");
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
                    findUser($scope.treedata[0].children[0].id);
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
            findUser(sel.id);
        };
        $scope.opts = {
            // dirSelectable: false
        }
        initSelectData();
    }

    function findUser(node_id){
        $rootScope.organize_parent_id=node_id;
        $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findUserList?organize_id="+node_id+"&name="+$scope.name+"&login_name="+$scope.login_name+"&phone="+$scope.phone).success(function(response){
            $scope.userData=response.data;
        });
    }

    if($rootScope.organize_parent_id!=undefined&&$rootScope.organize_parent_id!=null&&$rootScope.organize_parent_id!=''){
        findUser($rootScope.organize_parent_id)
    }

    $scope.selectUser = function(data,event) {
        var action = event.target;
        var user_ids = [];
        var user_names = [];
        if($scope.user_ids!=undefined){
            user_ids = $scope.user_ids.toString().split(',');
            user_names = $scope.user_names.toString().split(',');
        }
        if(action.checked){//选中，就添加
            if(user_ids.indexOf(data.id) == -1){//不存在就添加
                user_ids.push(data.id);
                $scope.user_ids = user_ids;
                user_names.push(data.name)
                $scope.user_names = user_names;
            }
        }else{//去除就删除result里
            var idx = user_ids.indexOf(data.id);
            if( idx != -1){//不存在就添加
                $scope.user_ids.splice(idx,1);
                $scope.user_names.splice(idx,1);
            }
        }
        initSelectData();
    }


    $scope.delUser = function(data) {
        var idx = $scope.user_ids.indexOf(data.user_id);
        if( idx != -1){//不存在就添加
            $scope.user_ids.splice(idx,1);
            $scope.user_names.splice(idx,1);
        }
        initSelectData();
    }

    function initSelectData(){
        if($scope.user_ids!=undefined){
            $scope.selectData = [];
            for (let i = 0; i < $scope.user_ids.length; i++) {
                if($scope.user_ids[i]!=""){
                    $scope.selectData.push({"user_id":$scope.user_ids[i],"user_name":$scope.user_names[i]})
                }
            }
        }
    }

    //被选中条件：ng-checked的结果为true
    $scope.isSelected = function(id){
        if($scope.user_ids!=undefined){
            var user_ids = $scope.user_ids.toString();
            return user_ids.indexOf(id)!=-1;
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
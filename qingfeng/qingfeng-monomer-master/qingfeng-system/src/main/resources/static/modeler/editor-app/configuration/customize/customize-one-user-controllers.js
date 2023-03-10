
var CustomizeUserSelectOnePopupCtrl = ['$scope','$http','$rootScope', function($scope,$http,$rootScope) {

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
        $scope.saveOneUser($scope.user_id,$scope.user_name);
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
    }

    function findUser(node_id){
        console.log('------------=======================')
        console.log('node_id:'+node_id)
        $rootScope.organize_parent_id=node_id;
        $http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findUserList?organize_id="+node_id).success(function(response){
            $scope.userData=response.data;
            console.log($scope.userData)
        });
    }

    if($rootScope.organize_parent_id!=undefined&&$rootScope.organize_parent_id!=null&&$rootScope.organize_parent_id!=''){
        findUser($rootScope.organize_parent_id)
    }
    // $scope.organize_id=$rootScope.organize_id;
    // $scope.organize_name=$rootScope.organize_name;
    $scope.selectUser = function(data) {
        $scope.user_id = data.id;
        $scope.user_name = data.name;
        $rootScope.user_id = data.id;
        $rootScope.user_name = data.name;
        console.log($scope.user_id)
    }


    $scope.delUser = function(organize_id,organize_name) {
        $scope.user_id = "";
        $scope.user_name = "";
        $rootScope.user_id = "";
        $rootScope.user_name = "";
    }

    function fommat(arrayList,pidStr = "parent_id",idStr = "id",childrenStr = "children") {
        arrayList.push({'name':'????????????','id':'1','parent_id':'0','org_cascade':'org_1_','level_num':'0'})
        let listOjb = {}; // ????????????{key: obj}???????????????
        let treeList = []; // ?????????????????????????????????????????????
        // ??????????????????{key: obj}?????????????????????????????????
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
        // ??????pid???????????????????????????
        for (let j = 0; j < arrayList.length; j++) {
            // ????????????????????????
            let haveParent = listOjb[arrayList[j][pidStr]];
            if (haveParent) {
                // ?????????????????????children????????????????????????children??????
                !haveParent[childrenStr] && (haveParent[childrenStr] = []);
                // ????????????????????????
                haveParent[childrenStr].push(arrayList[j]);
            } else {
                // ??????????????????????????????????????????
                treeList.push(arrayList[j]);
            }
        }
        return treeList;
    };

}];
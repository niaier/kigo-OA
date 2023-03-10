/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Assignment
 */
var KisBpmAssignmentCtrl = [ '$scope', '$modal','$http', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/assignment-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];
var KisBpmAssignmentPopupCtrl = [ '$scope','$modal','$http','$rootScope', function($scope,$modal,$http,$rootScope) {

    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.assignment !== undefined
        && $scope.property.value.assignment !== null) 
    {
        $scope.assignment = $scope.property.value.assignment;
    } else {
        $scope.assignment = {};
    }

    if ($scope.assignment.candidateUsers == undefined || $scope.assignment.candidateUsers.length == 0)
    {
    	$scope.assignment.candidateUsers = [{value: ''}];
    }
    
    // Click handler for + button after enum value
    var userValueIndex = 1;
    $scope.addCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index + 1, 0, {value: 'value ' + userValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index, 1);
    };
    
    if ($scope.assignment.candidateGroups == undefined || $scope.assignment.candidateGroups.length == 0)
    {
    	$scope.assignment.candidateGroups = [{value: ''}];
    }
    
    var groupValueIndex = 1;
    $scope.addCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index + 1, 0, {value: 'value ' + groupValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index, 1);
    };

    $scope.save = function() {

		if($scope.type=='0'){
			$scope.property.value = {};
			handleAssignmentInput($scope);
			$scope.property.value.assignment = $scope.assignment;
			$scope.property.value.assign_type = $scope.type;
			$scope.updatePropertyInModel($scope.property);
			$scope.close();
		}

		//????????????----??????????????????
		if($scope.type=='1'){
			if($scope.assign_mode=='1'){//??????
				$scope.assign_content = $scope.organize_id+"#"+$scope.organize_name;
			}else if($scope.assign_mode=='2'){//?????????
				$scope.assign_content = $scope.group_id;
			}else if($scope.assign_mode=='8'||$scope.assign_mode=='10'){//??????
				$scope.assign_content = $scope.user_ids+"#"+$scope.user_names;
			}else if($scope.assign_mode=='9'){//??????
				$scope.assign_content = $scope.user_id+"#"+$scope.user_name;
			}else if($scope.assign_mode=='11'){//??????
				$scope.assign_content = $scope.organize_ids+"#"+$scope.organize_names;
			}
			$scope.property.value = {};
			$scope.property.value.assign_type = $scope.type;
			$scope.property.value.assign_mode = $scope.assign_mode;
			$scope.updatePropertyInModel($scope.property);
			$scope.close();
		}
		$http({
			method: 'POST',
			url: ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/save",
			params:{
				node_key:nodeKey,
				model_id: model_id,
				type:$scope.type,
				assign_mode: $scope.assign_mode,
				assign_content:$scope.assign_content,
				id: $scope.id
			},
		}).then(function successCallback(response) {
		}, function errorCallback(response) {
			console.log("??????????????????????????????")
		});


    };

    // Close button handler
    $scope.close = function() {
    	handleAssignmentInput($scope);
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
    
    var handleAssignmentInput = function($scope) {
    	if ($scope.assignment.candidateUsers)
    	{
	    	var emptyUsers = true;
	    	var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateUsers.length; i++)
	        {
	        	if ($scope.assignment.candidateUsers[i].value != '')
	        	{
	        		emptyUsers = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateUsers.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyUsers)
	        {
	        	$scope.assignment.candidateUsers = undefined;
	        }
    	}
        
    	if ($scope.assignment.candidateGroups)
    	{
	        var emptyGroups = true;
	        var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateGroups.length; i++)
	        {
	        	if ($scope.assignment.candidateGroups[i].value != '')
	        	{
	        		emptyGroups = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateGroups.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyGroups)
	        {
	        	$scope.assignment.candidateGroups = undefined;
	        }
    	}
    };

	/*----------------------??????????????????------------------------------*/
	//??????????????????????????????????????????
	var nodeKey = $rootScope.nodeKey;
	var model_id=$rootScope.modelData.modelId;
	$scope.id = "";
	$scope.type="0";
	$scope.assign_mode="";
	$scope.assign_content="";
	//????????????????????????
	getActivitiAssignment();
	function getActivitiAssignment(){
		$http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findActivitiAssignment?node_key="+nodeKey+"&model_id="+model_id).success(function(response){
			console.log(response.data);
			if(response.data!=''&&response.data!=null&&response.data!=undefined){
				$scope.id=response.data.id;
				$scope.type=response.data.type;
				$scope.assign_mode=response.data.assign_mode;
				$scope.assign_content=response.data.assign_content;
				if(response.data.type=='1') {
					if (response.data.assign_mode == '1') {
						$scope.organize_id = response.data.assign_content.split('#')[0];
						$scope.organize_name = response.data.assign_content.split('#')[1];
					} else if (response.data.assign_mode == '2') {
						$scope.group_id = $scope.assign_content;
					} else if (response.data.assign_mode == '8'||response.data.assign_mode == '10') {
						$scope.user_ids = response.data.assign_content.split('#')[0];
						$scope.user_names = response.data.assign_content.split('#')[1];
					} else if (response.data.assign_mode == '9') {
						$scope.user_id = response.data.assign_content.split('#')[0];
						$scope.user_name = response.data.assign_content.split('#')[1];
					} else if (response.data.assign_mode == '11') {
						$scope.organize_ids = response.data.assign_content.split('#')[0];
						$scope.organize_names = response.data.assign_content.split('#')[1];
					}
				}
			}else{
				$scope.organize_id = "";
				$scope.organize_name = "";
				$scope.user_id = "";
				$scope.user_name = "";
			}
		});
	}


	$scope.selectAssignModee = function(assign_mode){
		$scope.assign_mode = assign_mode;
	}

	$scope.group_id="";
	$scope.$watch("assign_mode", function(n){
		if(n=='2'){//???????????????
			getGroupList()
		}
	})
	//???????????????
	function getGroupList(){
		$http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findGroupList").success(function(response){
			if(response.data!=''&&response.data!=null&&response.data!=undefined){
				$scope.groupList=response.data;
			}
		});
	}
	//?????? selectAssignmentGroup
	$scope.selectAssignmentGroup = function(group_id){
		$scope.group_id= group_id;
	}


	//?????????????????????
	$scope.selectMoreOrganize = function(){
		if($scope.organize_ids!=undefined){
			if(!Array.isArray($scope.organize_ids)){
				$scope.selectData=[];
				$scope.organize_ids = $scope.organize_ids.split(',');
				$scope.organize_names = $scope.organize_names.split(',');
				for (let i = 0; i < $scope.organize_ids.length; i++) {
					if($scope.organize_ids[i]!=""){
						$scope.selectData.push({"organize_id":$scope.organize_ids[i],"organize_name":$scope.organize_names[i]})
					}
				}
			}
		}
		var opts = {
			template:  'editor-app/configuration/customize/customize-more-organize.html?version=' + Date.now(),
			scope: $scope
		};
		// Open the dialog
		$modal(opts);

	}

	$scope.saveMoreOrganize = function (organize_ids,organize_names){
		$scope.organize_ids = organize_ids;
		$scope.organize_names = organize_names;
	}

	//?????????????????????
	$scope.selectOneOrganize = function(){
		var opts = {
			template:  'editor-app/configuration/customize/customize-one-organize.html?version=' + Date.now(),
			scope: $scope
		};
		// Open the dialog
		$modal(opts);
	}

	$scope.saveOneOrganize = function (organize_id,organize_name){
		$scope.organize_id = organize_id;
		$scope.organize_name = organize_name;
	}


	//?????????????????????
	$scope.selectMoreUser = function(){
		if($scope.user_ids!=undefined){
			if(!Array.isArray($scope.user_ids)){
				$scope.selectData=[];
				$scope.user_ids = $scope.user_ids.split(',');
				$scope.user_names = $scope.user_names.split(',');
				for (let i = 0; i < $scope.user_ids.length; i++) {
					if($scope.user_ids[i]!=""){
						$scope.selectData.push({"user_ids":$scope.user_ids[i],"user_names":$scope.user_names[i]})
					}
				}
			}
		}
		var opts = {
			template:  'editor-app/configuration/customize/customize-more-user.html?version=' + Date.now(),
			scope: $scope
		};
		// Open the dialog
		$modal(opts);
	}

	$scope.saveMoreUser = function (user_ids,user_names){
		$scope.user_ids = user_ids;
		$scope.user_names = user_names;
	}


	//?????????????????????
	$scope.selectOneUser = function(){
		var opts = {
			template:  'editor-app/configuration/customize/customize-one-user.html?version=' + Date.now(),
			scope: $scope
		};
		// Open the dialog
		$modal(opts);
	}

	$scope.saveOneUser = function (user_id,user_name){
		$scope.user_id = user_id;
		$scope.user_name = user_name;
	}


	/*---------------------???????????????????????????????????????--------------------*/
	//???????????????
	$scope.gridData = [];//????????????
	$scope.gridDataName = 'gridData';
	$scope.selectTitle = '???????????????';
	$scope.columnData = [];//???????????????
	$scope.columnDataName = 'columnData';
	$scope.selectType = 0;//0-????????????1-????????????2-?????????
	$scope.totalServerItems = 0;//???????????????
	//???????????????
	$scope.pagingOptions = {
		pageSizes: [10, 20, 30],//page ???????????????
		pageSize: 10, //????????????
		currentPage: 1, //??????????????????
	};

	//$scope.getPagedDataAsync($scope.pagingOptions.currentPage, $scope.pagingOptions.pageSize);
	//????????????
	//??????????????????
	$scope.$watch('pagingOptions', function (newValue, oldValue) {
		console.log("??????")
		$scope.getPagedDataAsync($scope.pagingOptions.currentPage, $scope.pagingOptions.pageSize);
	},true);

	//???????????????????????????????????????
	$scope.$watch('selectType', function (newValue, oldValue) {
		if(newValue != oldValue){
			//console.log("??????")
			$scope.pagingOptions.currentPage = 1;
			$scope.getPagedDataAsync($scope.pagingOptions.currentPage, $scope.pagingOptions.pageSize);
		}
	},true);

	//????????????????????????
	$scope.getPagedDataAsync = function(pageNum, pageSize){
		var url;
		if($scope.selectType == 2){
			url = '/activiti/assignment/findOrganizeListPage';
			$scope.columnData = $scope.groupColumns;
		}else{
			url = '/activiti/assignment/findUserListPage';
			$scope.columnData = $scope.userColumns;
		}
		//console.log(url)
		$http({
			method: 'GET',
			url: ACTIVITI.CONFIG.activitiRoot+url,
			params:{
				'pageNum': pageNum,
				'pageSize': pageSize
			},
		}).then(function successCallback(response) {
			console.log(response.data);
			$scope.gridData = response.data.data;
			$scope.totalServerItems = response.data.count;
		}, function errorCallback(response) {
			// ????????????????????????
			$scope.gridData = [];
			$scope.totalServerItems = 0;
		});
	}
	//??????????????????
	$scope.gridOptions = {
		data: $scope.gridDataName,
		multiSelect: false,//????????????
		enablePaging: true,
		pagingOptions: $scope.pagingOptions,
		totalServerItems: 'totalServerItems',
		i18n:'zh-cn',
		showFooter: true,
		showSelectionCheckbox: false,
		columnDefs : $scope.columnDataName,
		beforeSelectionChange: function (event) {
			var data = event.entity.id;
			if($scope.selectType == 0){//????????????
				event.entity.checked = !event.selected;
				$scope.assignment.assignee = data;
			}else if($scope.selectType == 1){//?????????
				var obj = {value: data};
				if(!array_contain($scope.assignment.candidateUsers, obj.value)){
					console.log($scope.assignment.candidateUsers)
					console.log($scope.assignment.candidateUsers.length)
					if($scope.assignment.candidateUsers[0].value==''||$scope.assignment.candidateUsers[0].value==null){
						$scope.assignment.candidateUsers[0]=obj;
					}else{
						$scope.assignment.candidateUsers.push(obj);
					}
				}
			}else if($scope.selectType == 2){//?????????
				var obj = {value: data};
				if(!array_contain($scope.assignment.candidateGroups, obj.value)){
					console.log($scope.assignment.candidateGroups)
					console.log($scope.assignment.candidateGroups.length)
					if($scope.assignment.candidateGroups[0].value==''||$scope.assignment.candidateGroups[0].value==null){
						$scope.assignment.candidateGroups[0].value=obj.value;
					}else{
						$scope.assignment.candidateGroups.push(obj);
					}
				}
			}
			return true;
		}
	};

	//???????????????
	$scope.getGroupData = function(data){
		var prefix = ['${projectId}_','${bankEnterpriseId}_','${coreEnterpriseId}_','${chainEnterpriseId}_'];
		var result = prefix[data.enterpriseType] + data.roleCode;
		return result;
	};

	//?????????????????????
	$scope.userColumns = [
		{
			field : 'id',
			type:'string',
			displayName : '??????Id',
			minWidth: 100,
			width : '25%'
		},
		{
			field : 'name',
			displayName : '??????',
			minWidth: 100,
			width : '25%'
		},
		{
			field : 'login_name',
			displayName : '?????????',
			minWidth: 100,
			width : '25%'
		},
		{
			field : 'sex',
			displayName : '??????',
			minWidth: 100,
			width : '25%',
			cellTemplate : '<span>{{row.entity.sex==1?"???":"???"}}</span>',//??????cell
		}
	];

	//????????????????????????
	$scope.groupColumns = [
		{
			field : 'id',
			type:'number',
			displayName : '???Id',
			minWidth: 100,
			width : '30%'
		},
		{
			field : 'name',
			displayName : '?????????',
			minWidth: 100,
			width : '70%'
		}
	];

	$scope.current_assignment="";
	//?????????(?????????)
	$scope.selectAssignee = function (n) {
		$scope.selectType = 0;
		$scope.selectTitle = '???????????????';
		//???????????????
		if(n!=''&&n!=null&&n!=undefined){
			$http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findUserInfo?id="+n).success(function(response){
				$scope.current_assignment="????????????"+response.data.name;
			});
		}else{
			$scope.current_assignment="";
		}
	};

	//?????????
	$scope.selectCandidate = function (n) {
		console.log(n);
		$scope.selectType = 1;
		$scope.selectTitle = '???????????????';
		//???????????????
		if(n!=''&&n!=null&&n!=undefined){
			$http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findUserInfo?id="+n).success(function(response){
				$scope.current_assignment="????????????"+response.data.name;
			});
		}else{
			$scope.current_assignment="";
		}
	};

	//?????????
	$scope.selectGroup = function (n) {
		console.log(n);
		$scope.selectType = 2;
		$scope.selectTitle = '???????????????';
		//???????????????
		if(n!=''&&n!=null&&n!=undefined){
			$http.get(ACTIVITI.CONFIG.activitiRoot+"/activiti/assignment/findOrganizeInfo?id="+n).success(function(response){
				$scope.current_assignment="????????????"+response.data.name;
			});
		}else{
			$scope.current_assignment="";
		}
	};

}];
//??????----???????????? contains ???????????????
function array_contain(array, obj){
	for (var i = 0; i < array.length; i++){
		if (array[i].value == obj)//????????????????????????????????????????????????????????????===
			return true;
	}
	return false;
}

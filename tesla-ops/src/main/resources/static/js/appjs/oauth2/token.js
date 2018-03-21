var prefix = "/sys/oauth2";
$(function() {
	load();
});

function load() {
	$('#tokenTable').bootstrapTable({
		method : 'get',
		url : prefix + "/listToken",
		iconSize : 'outline',
		toolbar : '#exampleToolbar',
		striped : true,
		dataType : "json",
		pagination : true,
		singleSelect : false,
		pageSize : 10,
		pageNumber : 1,
		showColumns : false,
		sidePagination : "server",
		queryParams : function(params) {
			return {
				limit : params.limit,
				offset : params.offset
			};
		},
		columns : [ {
			checkbox : true
		}, {
			field : 'tokenId',
			title : '令牌Id'
		}, {
			field : 'username',
			title : '用户名'
		}, {
			field : 'authenticationId',
			title : '授权Id'
		}, {
			field : 'clientId',
			title : '客户端Id'
		}, {
			field : 'tokenType',
			title : '令牌类型'
		}, {
			field : 'tokenExpiredSeconds',
			title : '失效时间'
		}, {
			field : 'refreshTokenExpiredSeconds',
			title : '刷新失效'
		} ]
	});
}
function reLoad() {
	$('#tokenTable').bootstrapTable('refresh');
}

function batchRemove() {
	var rows = $('#tokenTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要失效的数据");
		return;
	}
	layer.confirm("确认要失效选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	}, function() {
		var ids = new Array();
		$.each(rows, function(i, row) {
			ids[i] = row['tokenId'];
		});
		console.log(ids);
		$.ajax({
			type : 'POST',
			data : {
				"ids" : ids
			},
			url : prefix + '/batchRevoke',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	}, function() {
	});
}
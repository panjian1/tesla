var prefix = "/filter/rule";
$(function() {
	load();
});

function load() {
	$('#ruleTable')
			.bootstrapTable(
					{
						method : 'get',
						url : prefix + "/list",
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						expandColumn : '3',
						striped : true,
						dataType : "json",
						pagination : true,
						singleSelect : false,
						pageSize : 10,
						pageNumber : 1,
						sidePagination : "server",
						detailView : true,
						columns : [
								{
									checkbox : true
								},
								{
									field : 'id',
									title : '序号'
								},
								{
									field : 'filterType',
									title : '类型'
								},
								{
									field : 'url',
									title : 'url'
								},
								{
									field : 'rule',
									title : '规则'
								},
								{
									title : '操作',
									field : 'id',
									align : 'center',
									formatter : function(value, row, index) {
										var e = '<a class="btn btn-primary btn-sm '
												+ s_edit_h
												+ '" href="#" mce_href="#" title="编辑" onclick="edit(\''
												+ row.id
												+ '\')"><i class="fa fa-edit"></i></a> ';
										var d = '<a class="btn btn-warning btn-sm" href="#" title="删除"  mce_href="#" onclick="remove(\''
												+ row.id
												+ '\')"><i class="fa fa-remove"></i></a> ';
										return e + d;
									}
								} ]
					});
}
function reLoad() {
	$('#ruleTable').bootstrapTable('refresh');
}

function add() {
	layer.open({
		type : 2,
		title : '添加策略',
		maxmin : true,
		shadeClose : true,
		area : [ '1300px', '700px' ],
		content : prefix + '/add'
	});
}
function remove(id) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix + "/remove",
			type : "post",
			data : {
				'id' : id
			},
			success : function(r) {
				if (r.code === 0) {
					layer.msg("删除成功");
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	})

}
function edit(id) {
	layer.open({
		type : 2,
		title : '策略',
		maxmin : true,
		shadeClose : true,
		area : [ '1300px', '700px' ],
		content : prefix + '/edit/' + id
	});
}
function batchRemove() {
	var rows = $('#ruleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要删除的数据");
		return;
	}
	layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	}, function() {
		var ids = new Array();
		$.each(rows, function(i, row) {
			ids[i] = row['routeId'];
		});
		$.ajax({
			type : 'POST',
			data : {
				"ids" : ids
			},
			url : prefix + '/batchRemove',
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
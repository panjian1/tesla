var prefix = "/sys/log"
$(function() {
  load();
});
$('#exampleTable').on('load-success.bs.table', function(e, data) {
  if (data.total && !data.rows.length) {
    $('#exampleTable').bootstrapTable('selectPage').bootstrapTable('refresh');
  }
});

function load() {
  $('#exampleTable').bootstrapTable({
    method: 'get',
    url: prefix + "/list",
    iconSize: 'outline',
    striped: true,
    dataType: "json",
    pagination: true,
    singleSelect: false,
    pageSize: 10,
    pageNumber: 1,
    sidePagination: "server",
    queryParams: function(params) {
      return {
        limit: params.limit,
        offset: params.offset,
        name: $('#searchName').val(),
        sort: 'gmt_create',
        order: 'desc',
        operation: $("#searchOperation").val(),
        username: $("#searchUsername").val()
      };
    },
    columns: [{
      checkbox: true
    }, {
      field: 'id', // 列字段名
      title: '序号' // 列标题
    }, {
      field: 'userId',
      title: '用户Id'
    }, {
      field: 'username',
      title: '用户名'
    }, {
      field: 'operation',
      title: '操作'
    }, {
      field: 'time',
      title: '用时'
    }, {
      field: 'method',
      title: '方法'
    }, {
      field: 'ip',
      title: 'IP地址'
    }, {
      field: 'gmtCreate',
      title: '创建时间'
    }, {
      title: '操作',
      field: 'id',
      align: 'center',
      formatter: function(value, row, index) {
        var d = '<a class="btn btn-warning btn-sm" href="#" title="删除"  mce_href="#" onclick="remove(\'' + row.id + '\')"><i class="fa fa-remove"></i></a> ';
        return d;
      }
    }]
  });
}
function reLoad() {
  $('#exampleTable').bootstrapTable('refresh');
}
function remove(id) {
  layer.confirm('确定要删除选中的记录？', {
    btn: ['确定', '取消']
  }, function() {
    $.ajax({
      url: prefix + "/remove",
      type: "post",
      data: {
        'id': id
      },
      beforeSend: function(request) {
        index = layer.load();
      },
      success: function(r) {
        if (r.code == 0) {
          layer.close(index);
          layer.msg(r.msg);
          reLoad();
        } else {
          layer.msg(r.msg);
        }
      }
    });
  })
}
function batchRemove() {
  var rows = $('#exampleTable').bootstrapTable('getSelections');
  if (rows.length == 0) {
    layer.msg("请选择要删除的数据");
    return;
  }
  layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
    btn: ['确定', '取消']
  }, function() {
    var ids = new Array();
    $.each(rows, function(i, row) {
      ids[i] = row['id'];
    });
    $.ajax({
      type: 'POST',
      data: {
        "ids": ids
      },
      url: prefix + '/batchRemove',
      success: function(r) {
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
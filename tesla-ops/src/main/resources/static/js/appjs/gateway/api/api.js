var prefix = "gateway/api";
$(function() {
  load();
});

function load() {
  $('#routeTable').bootstrapTable({
    method: 'get',
    url: prefix + "/list",
    striped: true,
    dataType: "json",
    pagination: true,
    singleSelect: false,
    pageSize: 10,
    pageNumber: 1,
    sidePagination: "server",
    detailView: true,
    columns: [{
      checkbox: true
    }, {
      field: 'id',
      title: '序号'
    }, {
      field: 'name',
      title: 'API名称'
    }, {
      field: 'describe',
      title: 'API描述'
    }, {
      field: 'url',
      title: '请求路径'
    }, {
      field: 'groupName',
      title: 'API分组'
    }, {
      field: 'routeType',
      title: '路由模式'
    }, {
      title: '操作',
      field: 'id',
      align: 'center',
      formatter: function(value, row, index) {
        var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="javascript:void(0)" mce_href="#" title="编辑" onclick="edit(\'' + row.id + '\')"><i class="fa fa-edit"></i></a> ';
        var d = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="javascript:void(0)" mce_href="#" title="删除" onclick="remove(\'' + row.id + '\')"><i class="fa fa-remove"></i></a> ';
        return e + d;
      }
    }],
    onExpandRow: function(index, row, $detail) {
      if (row.routeType == 'Rpc') {
        chirdTable(index, row, $detail);
      }
    }
  });
}
function reLoad() {
  $('#routeTable').bootstrapTable('refresh');
}
function chirdTable(index, row, $detail) {
  var cur_table = $detail.html('<table class="table table-striped table-bordered table-hover"></table>').find('table');
  var rows = [];
  rows.push(row);
  $(cur_table).bootstrapTable({
    columns: [{
      field: 'rpc',
      title: 'Rpc服务',
      formatter: function(value, row, index) {
        if (row.protoContext != null) {
          return "gRPC";
        } else if (row.inputParam != null) { return "dubbo" }
      }
    }, {
      field: 'serviceName',
      title: '接口名'
    }, {
      field: 'methodName',
      title: '方法名'
    }, {
      field: 'serviceGroup',
      title: '组别'
    }, {
      field: 'serviceVersion',
      title: '版本'
    }, {
      title: '入参类型（grpc）',
      formatter: function(value, row, index) {
        return 'Proto字节流未展示'
      }
    }, {
      field: 'inputParam',
      title: '入参类型（dubbo）'
    }],
    data: rows
  });
}

function add() {
  var url = prefix + '/add';
  loadURL(url, $('#content'));
}
function edit(id) {
  var url = prefix + '/edit/' + id;
  loadURL(url, $('#content'));
}

function remove(id) {
  $.SmartMessageBox({
    title: "<i class='fa fa-sign-out txt-color-orangeDark'></i> 确定要删除选中的记录？",
    buttons: '[No][Yes]'
  }, function(ButtonPressed) {
    if (ButtonPressed == "Yes") {
      setTimeout(sureremove, 1000);
    }
  });
  function sureremove() {
    $.ajax({
      url: prefix + "/remove",
      type: "post",
      data: {
        'id': id
      },
      success: function(data) {
        loadURL(prefix, $('#content'));
      }
    });
  }
}
function batchRemove() {
  var rows = $('#routeTable').bootstrapTable('getSelections');
  if (rows.length == 0) {
    $.SmartMessageBox({
      title: "<i class='fa fa-sign-out txt-color-orangeDark'></i> 请选择要删除的记录？",
      buttons: '[Yes]'
    });
    return;
  }
  $.SmartMessageBox({
    title: "<i class='fa fa-sign-out txt-color-orangeDark'></i> 确定要删除选中的记录？",
    buttons: '[No][Yes]'
  }, function(ButtonPressed) {
    if (ButtonPressed == "Yes") {
      setTimeout(sureremove, 1000);
    }
  });
  function sureremove() {
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
        loadURL(prefix, $('#content'));
      }
    });
  }
}
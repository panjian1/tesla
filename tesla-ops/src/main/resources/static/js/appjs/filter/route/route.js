var prefix = "filter/route";
$(function() {
  load();
});

function load() {
  $('#routeTable').bootstrapTable({
    method: 'get',
    url: prefix + "/list",
    expandColumn: '3',
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
      field: 'routeId',
      title: '序号'
    }, {
      field: 'fromPath',
      title: '源路径'
    }, {
      field: 'toHostport',
      title: '目标地址'
    }, {
      field: 'toPath',
      title: '目标路径'
    }, {
      field: 'serviceId',
      title: '服务ID'
    }, {
      title: '操作',
      field: 'routeId',
      align: 'center',
      formatter: function(value, row, index) {
        var e = '<a class="btn btn-primary btn-sm ' + s_edit_h + '" href="javascript:void(0)" mce_href="#" title="编辑" onclick="edit(\'' + row.routeId + '\')"><i class="fa fa-edit"></i></a> ';
        var d = '<a class="btn btn-warning btn-sm ' + s_remove_h + '" href="javascript:void(0)" mce_href="#" title="删除" onclick="remove(\'' + row.routeId + '\')"><i class="fa fa-remove"></i></a> ';
        return e + d;
      }
    }],
    onExpandRow: function(index, row, $detail) {
      if (row.rpc) {
        chirdTable(index, row, $detail);
      }
    }
  });
}
function reLoad() {
  $('#routeTable').bootstrapTable('refresh');
}
function chirdTable(index, row, $detail) {
  var cur_table = $detail.html('<table></table>').find('table');
  var rows = [];
  rows.push(row);
  $(cur_table).bootstrapTable({
    columns: [{
      field: 'rpc',
      title: 'Rpc服务',
      formatter: function(value, row, index) {
        if (value) {
          return "是";
        } else {
          return "否"
        }
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
      ids[i] = row['routeId'];
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
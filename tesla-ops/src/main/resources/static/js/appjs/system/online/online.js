var prefix = "/sys/online"
$(function() {
  load();
});

function load() {
  $('#onlineTable').bootstrapTable({
    method: 'get', // 服务器数据的请求方式 get or post
    url: prefix + "/list", // 服务器数据的加载地址
    iconSize: 'outline',
    striped: true, // 设置为true会有隔行变色效果
    dataType: "json", // 服务器返回的数据类型
    pagination: true, // 设置为true会在底部显示分页条
    singleSelect: false, // 设置为true将禁止多选
    pageSize: 10, // 如果设置了分页，每页数据条数
    pageNumber: 1, // 如果设置了分布，首页页码
    showColumns: false, // 是否显示内容下拉框（选择显示的列）
    sidePagination: "client", // 设置在哪里进行分页，可选值为"client" 或者
    queryParams: function(params) {
      return {
        limit: params.limit,
        offset: params.offset,
        name: $('#searchName').val()
      };
    },
    columns: [{
      checkbox: true
    }, {
      field: 'id', // 列字段名
      title: '序号' // 列标题
    }, {
      field: 'username',
      title: '用户名'
    }, {
      field: 'host',
      title: '主机'
    }, {
      field: 'startTimestamp',
      title: '登录时间'
    }, {
      field: 'lastAccessTime',
      title: '最后访问时间'
    }, {
      field: 'timeout',
      title: '过期时间'
    }, {
      field: 'status',
      title: '状态',
      align: 'center',
      formatter: function(value, row, index) {
        if (value == 'on_line') {
          return '<span class="label label-success">在线</span>';
        } else if (value == 'off_line') { return '<span class="label label-primary">离线</span>'; }
      }
    }, {
      title: '操作',
      field: 'id',
      align: 'center',
      formatter: function(value, row, index) {
        var d = '<a class="btn btn-warning btn-sm" href="javascript:void(0)" title="删除"  mce_href="#" onclick="forceLogout(\'' + row.id + '\')"><i class="fa fa-remove"></i></a> ';
        return d;
      }
    }]
  });
}
function reLoad() {
  $('#onlineTable').bootstrapTable('refresh');
}
function forceLogout(id) {
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
      url: prefix + "/forceLogout/" + id,
      type: "post",
      data: {
        'id': id
      },
      success: function(data) {
        window.location.reload();
      }
    });
  }
}
$(document).ready(function() {
  pageSetUp();
  var pagefunction = function() {
    loadScript("js/plugin/bootstrap-wizard/jquery.bootstrap.wizard.min.js", runBootstrapWizard);
    function runBootstrapWizard() {
      var $validator = $("#routeForm").validate({
        rules: {
          fromPath: {
            required: true
          },
          serviceName: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                return isRpc == 1;
              }
            }
          },
          methodName: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                return isRpc == 1;
              }
            }
          },
          serviceGroup: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                return isRpc == 1;
              }
            }
          },
          serviceVersion: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                return isRpc == 1;
              }
            }
          },
          zipFile: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                return isRpc == 1;
              }
            }
          },
          serviceFileName: {
            required: {
              depends: function(value, element) {
                var isRpc = $('#rpc').val();
                if (isRpc == 1) {
                  return $('#zipFile').val() != null;
                } else {
                  return false;
                }
              }
            }
          }
        },
        messages: {
          fromPath: {
            required: "请输入路由路径！"
          },
          serviceName: {
            required: "请输入服务名！"
          },
          methodName: {
            required: "请输入方法名！"
          },
          serviceGroup: {
            required: "请输入组别！"
          },
          serviceVersion: {
            required: "请输入版本！"
          },
          zipFile: {
            required: "请上传proto目录文件！"
          },
          serviceFileName: {
            required: "上传proto目录文件，需要指定目录中的服务定义文件名！"
          }
        },
        highlight: function(element) {
          $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        unhighlight: function(element) {
          $(element).closest('.form-group').removeClass('has-error').addClass('has-success');
        },
        errorElement: 'span',
        errorClass: 'help-block',
        errorPlacement: function(error, element) {
          if (element.parent('.input-group').length) {
            error.insertAfter(element.parent());
          } else {
            error.insertAfter(element);
          }
        }
      });
      $('#routeWizard').bootstrapWizard({
        'tabClass': 'form-wizard',
        'onNext': function(tab, navigation, index) {
          var $valid = $("#routeForm").valid();
          if (!$valid) {
            $validator.focusInvalid();
            return false;
          } else {
            $('#bootstrap-wizard-1').find('.form-wizard').children('li').eq(index - 1).addClass('complete');
            $('#bootstrap-wizard-1').find('.form-wizard').children('li').eq(index - 1).find('.step').html('<i class="fa fa-check"></i>');
          }
        },
        'onTabClick': function(tab, navigation, index) {
          var $valid = $("#routeForm").valid();
          if (!$valid) {
            $validator.focusInvalid();
            return false;
          } else {
            $('#bootstrap-wizard-1').find('.form-wizard').children('li').eq(index - 1).addClass('complete');
            $('#bootstrap-wizard-1').find('.form-wizard').children('li').eq(index - 1).find('.step').html('<i class="fa fa-check"></i>');
          }
        },
        'onFinish': function(tab, navigation, index) {
          var $valid = $("#routeForm").valid();
          if (!$valid) {
            $validator.focusInvalid();
            return false;
          } else {
            loadScript("js/plugin/jquery-form/jquery-form.min.js", function() {
              $("#routeForm").ajaxSubmit({
                type: "POST",
                url: "/filter/route/save",
                dataType: 'json',
                data: $('#routeForm').serialize(),
                error: function(request) {
                  parent.layer.alert("Connection error");
                },
                success: function() {
                  loadURL("filter/route", $('#content'));
                }
              });
            });
          }
        }
      });
    }
  }
  pagefunction();
});

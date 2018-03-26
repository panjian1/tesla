$(document).ready(function(){
	var prefix = "/sys/menu"
	pageSetUp();
    function goback(){
	  loadURL("sys/menu", $('#content'));
    }
	var pagefunction = function() {
		var $menuform = $("#menuform").validate({
			rules : {
				name : {
					required : true
				},
				type : {
					required : true
				}
			},
			messages : {
				name : {
					required : "请输入菜单名"
				},
				type : {
					required : "请选择菜单类型"
				}
			},
			submitHandler : function(form) {
				$(form).ajaxSubmit({
					cache : true,
					type : "POST",
					url : prefix + "/save",
					data : $('#menuform').serialize(),
					async : false,
					success : function() {
						$("#menuform").addClass('submited');
					}
				});
			},
			errorPlacement : function(error, element) {
				alert("test");
				error.insertAfter(element.parent());
			}
		});
	};
	loadScript("js/plugin/jquery-form/jquery-form.min.js", pagefunction);
});
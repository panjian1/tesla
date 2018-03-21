$().ready(function() {
	$('#scope').multiselect();
	$('#grantTypes').multiselect();
	validateRule();
});
$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	$("#oauth2Form").ajaxSubmit({
		type : "POST",
		url : "/sys/oauth2/save",
		dataType : 'json',
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name);
				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}

		}
	});
}
function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#oauth2Form").validate({
		rules : {
			clientId : {
				required : true
			},
			name : {
				required : true
			},
			clientSecret : {
				required : true
			},
			scope : {
				required : true
			},
			grantTypes : {
				required : true
			},
			accessTokenValidity : {
				required : true
			},
			refreshTokenValidity : {
				required : true
			},
			trusted : {
				required : true
			},
			redirectUri : {
				required : true
			}
		},
		messages : {
			clientId : {
				required : icon + "请输入客户端ID！"
			},
			name : {
				required : icon + "请输入客户端命名！"
			},
			clientSecret : {
				required : icon + "请输入客户端安全码！"
			},
			scope : {
				required : icon + "请选择授权范围！"
			},
			grantTypes : {
				required : icon + "请选择授权类型！"
			},
			accessTokenValidity : {
				required : icon + "请输入AccessToken有效时长！"
			},
			refreshTokenValidity : {
				required : icon + "请输入RefreshToken有效时长！"
			},
			trusted : {
				required : icon + "请选择是否信任！"
			},
			redirectUri : {
				required : icon + "请输入重定向URL！"
			}
		}
	})
}
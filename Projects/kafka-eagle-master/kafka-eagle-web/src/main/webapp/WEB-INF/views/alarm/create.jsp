<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Alarm - KafkaEagle</title>
<jsp:include page="../public/plus/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
</head>

<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
<div id="layoutSidenav">
	<div id="layoutSidenav_nav">
		<jsp:include page="../public/plus/navbar.jsp"></jsp:include>
	</div>
	<div id="layoutSidenav_content">
		<main>
			<div class="container-fluid">
				<h1 class="mt-4">AlarmCluster</h1>
				<ol class="breadcrumb mb-4">
					<li class="breadcrumb-item"><a href="#">AlarmCluster</a></li>
					<li class="breadcrumb-item active">Create</li>
				</ol>
				<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
					<i class="fas fa-info-circle"></i> <strong>Set different alarm modes, such as DingDing, WeChat, Mail etc.</strong>
				</div>
				<!-- content body -->
				<div class="row">
					<div class="col-lg-12">
						<div class="card mb-4">
							<div class="card-header">
								<i class="fas fa-server"></i> Mode Setting
							</div>
							<div class="card-body">
								<div class="row">
									<div class="col-lg-12">
										<form role="form" action="/alarm/create/form" method="post" onsubmit="return contextAlarmAddFormValid();return false;">
											<div class="form-group">
												<label>Alarm Type (*)</label>
												<select id="select2type" name="select2type" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
												<input id="ke_alarm_cluster_type" name="ke_alarm_cluster_type" type="hidden" />
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Select the cluster type you need to alarm .
												</label>
											</div>
											<div id="ke_alarm_server_div" class="form-group">
												<label>Server (*)</label>
												<textarea id="ke_server_alarm" name="ke_server_alarm" class="form-control" rows="3" placeholder="dn1:9092,dn2:9092,dn3:9092"></textarea>
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Such as kafka(dn1:9092,dn2:9092,dn3:9092) or zookeeper(dn1:2181,dn2:2181,dn3:2181) .
												</label>
											</div>
											<div id="ke_alarm_topic_div" style="display: none" class="form-group">
												<label>Topic (*)</label>
												<textarea id="ke_topic_alarm" name="ke_topic_alarm" class="form-control" rows="3"></textarea>
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Write topic and capacity alarm value(unit is byte), such as {"topic":"ke_alarm_topic","capacity":1024}.
												</label>
											</div>
											<div id="ke_alarm_producer_div" style="display: none" class="form-group">
												<label>Producer (*)</label>
												<textarea id="ke_producer_alarm" name="ke_producer_alarm" class="form-control" rows="3"></textarea>
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Write topic and speed alarm value(unit is msg/min), such as {"topic":"ke_alarm_topic","speed":"10000,20000"}.
												</label>
											</div>
											<div class="form-group">
												<label>Alarm Level (*)</label>
												<select id="select2level" name="select2level" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
												<input id="ke_alarm_cluster_level" name="ke_alarm_cluster_level" type="hidden" />
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Select the cluster level you need to alarm .
												</label>
											</div>
											<div class="form-group">
												<label>Alarm Max Times (*)</label>
												<select id="select2maxtimes" name="select2maxtimes" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
												<input id="ke_alarm_cluster_maxtimes" name="ke_alarm_cluster_maxtimes" type="hidden" />
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Select the cluster alarm max times you need to alarm .
												</label>
											</div>
											<div class="form-group">
												<label>Alarm Group (*)</label>
												<select id="select2group" name="select2group" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
												<input id="ke_alarm_cluster_group" name="ke_alarm_cluster_group" type="hidden" />
												<label for="inputError" class="control-label text-danger">
													<i class="fa fa-info-circle"></i> Select the cluster alarm group you need to alarm .
												</label>
											</div>
											<button type="submit" class="btn btn-success">Add</button>
											<div id="alert_create_message" style="display: none" class="alert alert-danger">
												<label>Oops! Please make some changes . (*) is required .</label>
											</div>
										</form>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</main>
		<jsp:include page="../public/plus/footer.jsp"></jsp:include>
	</div>
</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
	<jsp:param value="main/alarm/create.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextAlarmAddFormValid() {
		var ke_alarm_cluster_type = $("#ke_alarm_cluster_type").val();
		var ke_server_alarm = $("#ke_server_alarm").val();
		var ke_topic_alarm = $("#ke_topic_alarm").val();
		var ke_producer_alarm = $("#ke_producer_alarm").val();
		var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();
		var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();
		var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();

		if (ke_alarm_cluster_type.indexOf("Topic") > -1) {
			if (ke_alarm_cluster_type.length == 0 || ke_topic_alarm.length == 0 || ke_alarm_cluster_level.length == 0 || ke_alarm_cluster_group.length == 0 || ke_alarm_cluster_maxtimes.length == 0) {
				$("#alert_create_message").show();
				setTimeout(function() {
					$("#alert_create_message").hide()
				}, 3000);
				return false;
			}
		} else if (ke_alarm_cluster_type.indexOf("Producer") > -1) {
			if (ke_alarm_cluster_type.length == 0 || ke_producer_alarm.length == 0 || ke_alarm_cluster_level.length == 0 || ke_alarm_cluster_group.length == 0 || ke_alarm_cluster_maxtimes.length == 0) {
				$("#alert_create_message").show();
				setTimeout(function() {
					$("#alert_create_message").hide()
				}, 3000);
				return false;
			}
		} else {
			if (ke_alarm_cluster_type.length == 0 || ke_server_alarm.length == 0 || ke_alarm_cluster_level.length == 0 || ke_alarm_cluster_group.length == 0 || ke_alarm_cluster_maxtimes.length == 0) {
				$("#alert_create_message").show();
				setTimeout(function() {
					$("#alert_create_message").hide()
				}, 3000);
				return false;
			}
		}

		return true;
	}
</script>
</html>

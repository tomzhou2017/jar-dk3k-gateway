<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>pageTitle</title>
    <script type="text/javascript" src="/statics/js/commons/jquery-1.11.3.min.js"></script>
</head>
<body>

<form id="uploadForm">
    上传文件：
    <input type="file" name="file"/>
    <input type="button" value="上传" id="upload"/>
</form>

<a href="#" id="download">下载 </a>
</body>
<script>

    $(document).ready(function () {
        $("#upload").click(function () {
            var formData = new FormData($("#uploadForm")[0]);
            $.ajax({
                url: "${pageContext.request.contextPath}/excel/file/importFileDatas",
                type: "POST",
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (returndata) {
                    alert(returndata);
                },
                error: function (returndata) {
                    alert(returndata);
                }
            });
        });
        $("#download").click(function () {
            // 这个值动态获取
            var file = "file";
            window.open("${pageContext.request.contextPath}/excel/file/exportFileDatas?file=" + file);
        });

    });

</script>
</html>
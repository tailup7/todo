<%@ page
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>

<%@ taglib
    prefix="c"
    uri="jakarta.tags.core"
%>

<!DOCTYPE html>
<html lang="ja">

<head>
    <meta charset="UTF-8">
    <title>エラー</title>
</head>

<body>

<h1>処理中にエラーが発生しました</h1>

<p>
    HTTPステータス:
    <c:out
        value="${requestScope['jakarta.servlet.error.status_code']}"
    />
</p>

<p>
    詳細は管理者ログを確認してください。
</p>

<c:url
    var="listUrl"
    value="/todos"
/>

<p>
    <a href="${listUrl}">
        ToDo一覧へ戻る
    </a>
</p>

</body>
</html>

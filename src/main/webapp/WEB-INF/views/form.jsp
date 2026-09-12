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

    <c:choose>
        <c:when test="${mode eq 'edit'}">
            <title>ToDo編集</title>
        </c:when>
        <c:otherwise>
            <title>ToDo追加</title>
        </c:otherwise>
    </c:choose>

</head>

<body>

<c:choose>

    <c:when test="${mode eq 'edit'}">

        <h1>ToDo編集</h1>

        <c:url
            var="formUrl"
            value="/todos/update"
        />

    </c:when>

    <c:otherwise>

        <h1>ToDo追加</h1>

        <c:url
            var="formUrl"
            value="/todos/create"
        />

    </c:otherwise>

</c:choose>

<form
    method="post"
    action="${formUrl}">

    <input
        type="hidden"
        name="_csrf"
        value="${csrfToken}"
    >

    <c:if test="${mode eq 'edit'}">

        <input
            type="hidden"
            name="id"
            value="${todo.id}"
        >

    </c:if>

    <div>

        <label for="title">
            タイトル
        </label>

        <input
            id="title"
            name="title"
            type="text"
            maxlength="200"
            required
            value="<c:out value='${todo.title}'/>"
        >

        <c:if test="${not empty errors.title}">

            <p>
                <c:out
                    value="${errors.title}"
                />
            </p>

        </c:if>

    </div>

    <div>

        <label for="description">
            説明
        </label>

        <textarea
            id="description"
            name="description"
            maxlength="2000"
        ><c:out value="${todo.description}"/></textarea>

        <c:if test="${not empty errors.description}">

            <p>
                <c:out
                    value="${errors.description}"
                />
            </p>

        </c:if>

    </div>

    <div>
        <label for="status">状態</label>
    
        <select id="status" name="status" required>
            <c:forEach items="${statuses}" var="status">
                <option
                    value="${status}"
                    <c:if test="${status eq todo.status}">
                        selected
                    </c:if>
                >
                    <c:out value="${status.label}"/>
                </option>
            </c:forEach>
        </select>
    
        <c:if test="${not empty errors.status}">
            <p><c:out value="${errors.status}"/></p>
        </c:if>
    </div>

    <button type="submit">
        保存
    </button>

</form>

<c:url
    var="listUrl"
    value="/todos"
/>

<p>
    <a href="${listUrl}">
        一覧へ戻る
    </a>
</p>

</body>
</html>

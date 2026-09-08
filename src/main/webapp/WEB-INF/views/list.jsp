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
    <title>ToDo一覧</title>
</head>

<body>

<h1>ToDo一覧</h1>

<c:url
    var="newUrl"
    value="/todos/new"
/>

<p>
    <a href="${newUrl}">
        新しいToDoを追加
    </a>
</p>

<c:choose>

    <c:when test="${empty todos}">
        <p>ToDoはありません。</p>
    </c:when>

    <c:otherwise>

        <table border="1">

            <thead>
            <tr>
                <th>ID</th>
                <th>タイトル</th>
                <th>説明</th>
                <th>完了</th>
                <th>操作</th>
            </tr>
            </thead>

            <tbody>

            <c:forEach
                items="${todos}"
                var="todo">

                <tr>

                    <td>
                        <c:out
                            value="${todo.id}"
                        />
                    </td>

                    <td>
                        <c:out
                            value="${todo.title}"
                        />
                    </td>

                    <td>
                        <c:out
                            value="${todo.description}"
                        />
                    </td>

                    <td>
                        <c:choose>
                            <c:when
                                test="${todo.completed}">
                                完了
                            </c:when>
                            <c:otherwise>
                                未完了
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>

                        <c:url
                            var="editUrl"
                            value="/todos/edit">

                            <c:param
                                name="id"
                                value="${todo.id}"
                            />

                        </c:url>

                        <a href="${editUrl}">
                            編集
                        </a>

                        <c:url
                            var="deleteUrl"
                            value="/todos/delete"
                        />

                        <form
                            method="post"
                            action="${deleteUrl}"
                            style="display:inline">

                            <input
                                type="hidden"
                                name="_csrf"
                                value="${csrfToken}"
                            >

                            <input
                                type="hidden"
                                name="id"
                                value="${todo.id}"
                            >

                            <button type="submit">
                                削除
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

            </tbody>

        </table>

    </c:otherwise>

</c:choose>

</body>
</html>

@param String page
@param String message = ""

<!DOCTYPE html>
<html lang="en-US">
<head>
    <title>Java 25 Undertow</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="Author" content="Daniel Popiniuc">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/web/static/styles.min.css">
    <link rel="stylesheet" href="/web/static/common-sample1.css">
    <!-- Bootstrap components -->
    <link rel="stylesheet" href="/web/static/bootstrap/bootstrap.min.css">
    <script src="/web/static/bootstrap/bootstrap.bundle.min.js" defer></script>
    <!-- Fontawesome components -->
    <link rel="stylesheet" href="/web/static/fontawesome/fontawesome.min.css">
    <script src="/web/static/fontawesome/regular.min.js" defer></script>
    <script src="/web/static/fontawesome/solid.min.js" defer></script>
    <script src="/web/static/fontawesome/brands.min.js" defer></script>
    <script src="/web/static/fontawesome/fontawesome.min.js" defer></script>
</head>
<body>
    <nav>
        <a href="/?page=home">Home</a> | 
        <a href="/?page=login">Login</a>
    </nav>
    <main>
        @if(page.equals("login"))
            <h1>Login</h1>
            <form action="/login" method="POST">
                <input type="text" name="username" placeholder="Username" required>
                <input type="password" name="password" placeholder="Password" required>
                <button type="submit">Submit</button>
            </form>
        @else
            <h1>Welcome!</h1>
            <p>Status: ${message.isEmpty() ? "Not logged in" : message}</p>
        @endif
    </main>
    <footer>&#169; Copyright by Daniel Popiniuc - 2026</footer>
</body>
</html>
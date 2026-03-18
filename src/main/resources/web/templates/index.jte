@import gg.jte.Content
@param String page
@param String title
@param Content menu
@param Content content

<!DOCTYPE html>
<html lang="en-US">
<head>
    <title>${title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="Author" content="Daniel Popiniuc">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/web/static/styles.css">
    <link rel="stylesheet" href="/web/static/common-sample1.css">
    <!-- Bootstrap components -->
    <link rel="stylesheet" href="/web/static/BootStrap/5.3.8/bootstrap.min.css">
    <script src="/web/static/BootStrap/5.3.8/bootstrap.bundle.min.js" defer></script>
    <!-- Fontawesome components -->
    <link rel="stylesheet" href="/web/static/FontAwesome/7.2.0/fontawesome.min.css">
    <script src="/web/static/FontAwesome/7.2.0/brands.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/fontawesome.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/regular.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/solid.min.js" defer></script>
    <!-- Tabber components -->
    <script src="/web/static/Tabber/1.0.0/tabber.min.js" defer></script>
</head>
<body>
    <style type="text/css">.tabber{display:none;}</style>
    <header id="TitleContainer">
        <h1 id="PageTitle">${title}</h1>
    </header>
    <nav>
        <ul>${menu}</ul>
    </nav>
    <main>${content}</main>
    <footer>&#169; Copyright by Daniel Popiniuc - 2026</footer>
</body>
</html>
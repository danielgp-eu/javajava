@import gg.jte.Content
@param String page
@param Content content

<!DOCTYPE html>
<html lang="en-US">
<head>
    <title>JavaJava 25 Undertow</title>
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
        <h1 id="PageTitle">
@if(page.equals("SoftwareReleases"))
    Software Releases
@elseif(page.equals("FilesHashing"))
    File Hashes
@else
    Welcome!
@endif
        </h1>
    </header>
    <nav>
        <ul>
            <li><a href="?page=home"><i class="fa-solid fa-house-user"></i>Home</a></li>
            <li><a href="?page=SoftwareReleases"><i class="fa-brands fa-dev"></i>Releases</a></li>
            <li><a href="?page=FilesHashing"><i class="fa-solid fa-hashtag"></i>Hashing</a></li>
            <li><a href="?page=EnvironmentDetails"><i class="fa-solid fa-computer"></i>Environment</a></li>
        </ul>
    </nav>
    <main>${content}</main>
    <footer>&#169; Copyright by Daniel Popiniuc - 2026</footer>
</body>
</html>
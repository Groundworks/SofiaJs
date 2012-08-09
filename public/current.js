function includeCss(cssFilePath) {
    var css = document.createElement("link");

    css.rel  = "stylesheet";
    css.href = cssFilePath;

    document.body.appendChild(css);
}

function includeJs(jsFilePath) {
    var js = document.createElement("script");

    js.type = "text/javascript";
    js.src = jsFilePath;

    document.body.appendChild(js);
}
includeCss("/assets/stylesheets/main.css");

includeJs("https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js");
includeJs("/assets/javascripts/showdown.js");
includeJs("/assets/sitebox.js");

var posturl = siteboxhost + "/content"
var updateurl = siteboxhost + "/update";
var posturl = siteboxhost + "/content";

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
includeCss("http://sitebox.herokuapp.com/assets/stylesheets/main.css");

includeJs("https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js");
includeJs("http://sitebox.herokuapp.com/assets/javascripts/showdown.js");
includeJs("http://sitebox.herokuapp.com/assets/sitebox.js");

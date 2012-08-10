var siteboxhost = "http://sitebox.herokuapp.com";
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
includeCss(siteboxhost+"/assets/stylesheets/main.css");
includeJs(siteboxhost+"/assets/sitebox.js");

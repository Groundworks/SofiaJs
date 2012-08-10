var Showdown={};Showdown.converter=function(){var a,b,c,d=0;this.makeHtml=function(d){return a=new Array,b=new Array,c=new Array,d=d.replace(/~/g,"~T"),d=d.replace(/\$/g,"~D"),d=d.replace(/\r\n/g,"\n"),d=d.replace(/\r/g,"\n"),d="\n\n"+d+"\n\n",d=F(d),d=d.replace(/^[ \t]+$/mg,""),d=f(d),d=e(d),d=h(d),d=D(d),d=d.replace(/~D/g,"$$"),d=d.replace(/~T/g,"~"),d};var e=function(c){var c=c.replace(/^[ ]{0,3}\[(.+)\]:[ \t]*\n?[ \t]*<?(\S+?)>?[ \t]*\n?[ \t]*(?:(\n*)["(](.+?)[")][ \t]*)?(?:\n+|\Z)/gm,function(c,d,e,f,g){return d=d.toLowerCase(),a[d]=z(e),f?f+g:(g&&(b[d]=g.replace(/"/g,"&quot;")),"")});return c},f=function(a){a=a.replace(/\n/g,"\n\n");var b="p|div|h[1-6]|blockquote|pre|table|dl|ol|ul|script|noscript|form|fieldset|iframe|math|ins|del",c="p|div|h[1-6]|blockquote|pre|table|dl|ol|ul|script|noscript|form|fieldset|iframe|math";return a=a.replace(/^(<(p|div|h[1-6]|blockquote|pre|table|dl|ol|ul|script|noscript|form|fieldset|iframe|math|ins|del)\b[^\r]*?\n<\/\2>[ \t]*(?=\n+))/gm,g),a=a.replace(/^(<(p|div|h[1-6]|blockquote|pre|table|dl|ol|ul|script|noscript|form|fieldset|iframe|math)\b[^\r]*?.*<\/\2>[ \t]*(?=\n+)\n)/gm,g),a=a.replace(/(\n[ ]{0,3}(<(hr)\b([^<>])*?\/?>)[ \t]*(?=\n{2,}))/g,g),a=a.replace(/(\n\n[ ]{0,3}<!(--[^\r]*?--\s*)+>[ \t]*(?=\n{2,}))/g,g),a=a.replace(/(?:\n\n)([ ]{0,3}(?:<([?%])[^\r]*?\2>)[ \t]*(?=\n{2,}))/g,g),a=a.replace(/\n\n/g,"\n"),a},g=function(a,b){var d=b;return d=d.replace(/\n\n/g,"\n"),d=d.replace(/^\n/,""),d=d.replace(/\n+$/g,""),d="\n\n~K"+(c.push(d)-1)+"K\n\n",d},h=function(a){a=o(a);var b=t("<hr />");return a=a.replace(/^[ ]{0,2}([ ]?\*[ ]?){3,}[ \t]*$/gm,b),a=a.replace(/^[ ]{0,2}([ ]?\-[ ]?){3,}[ \t]*$/gm,b),a=a.replace(/^[ ]{0,2}([ ]?\_[ ]?){3,}[ \t]*$/gm,b),a=q(a),a=s(a),a=r(a),a=x(a),a=f(a),a=y(a),a},i=function(a){return a=u(a),a=j(a),a=A(a),a=m(a),a=k(a),a=B(a),a=z(a),a=w(a),a=a.replace(/  +\n/g," <br />\n"),a},j=function(a){var b=/(<[a-z\/!$]("[^"]*"|'[^']*'|[^'">])*>|<!(--.*?--\s*)+>)/gi;return a=a.replace(b,function(a){var b=a.replace(/(.)<\/?code>(?=.)/g,"$1`");return b=G(b,"\\`*_"),b}),a},k=function(a){return a=a.replace(/(\[((?:\[[^\]]*\]|[^\[\]])*)\][ ]?(?:\n[ ]*)?\[(.*?)\])()()()()/g,l),a=a.replace(/(\[((?:\[[^\]]*\]|[^\[\]])*)\]\([ \t]*()<?(.*?)>?[ \t]*((['"])(.*?)\6[ \t]*)?\))/g,l),a=a.replace(/(\[([^\[\]]+)\])()()()()()/g,l),a},l=function(c,d,e,f,g,h,i,j){j==undefined&&(j="");var k=d,l=e,m=f.toLowerCase(),n=g,o=j;if(n==""){m==""&&(m=l.toLowerCase().replace(/ ?\n/g," ")),n="#"+m;if(a[m]!=undefined)n=a[m],b[m]!=undefined&&(o=b[m]);else{if(!(k.search(/\(\s*\)$/m)>-1))return k;n=""}}n=G(n,"*_");var p='<a href="'+n+'"';return o!=""&&(o=o.replace(/"/g,"&quot;"),o=G(o,"*_"),p+=' title="'+o+'"'),p+=">"+l+"</a>",p},m=function(a){return a=a.replace(/(!\[(.*?)\][ ]?(?:\n[ ]*)?\[(.*?)\])()()()()/g,n),a=a.replace(/(!\[(.*?)\]\s?\([ \t]*()<?(\S+?)>?[ \t]*((['"])(.*?)\6[ \t]*)?\))/g,n),a},n=function(c,d,e,f,g,h,i,j){var k=d,l=e,m=f.toLowerCase(),n=g,o=j;o||(o="");if(n==""){m==""&&(m=l.toLowerCase().replace(/ ?\n/g," ")),n="#"+m;if(a[m]==undefined)return k;n=a[m],b[m]!=undefined&&(o=b[m])}l=l.replace(/"/g,"&quot;"),n=G(n,"*_");var p='<img src="'+n+'" alt="'+l+'"';return o=o.replace(/"/g,"&quot;"),o=G(o,"*_"),p+=' title="'+o+'"',p+=" />",p},o=function(a){function b(a){return a.replace(/[^\w]/g,"").toLowerCase()}return a=a.replace(/^(.+)[ \t]*\n=+[ \t]*\n+/gm,function(a,c){return t('<h1 id="'+b(c)+'">'+i(c)+"</h1>")}),a=a.replace(/^(.+)[ \t]*\n-+[ \t]*\n+/gm,function(a,c){return t('<h2 id="'+b(c)+'">'+i(c)+"</h2>")}),a=a.replace(/^(\#{1,6})[ \t]*(.+?)[ \t]*\#*\n+/gm,function(a,c,d){var e=c.length;return t("<h"+e+' id="'+b(d)+'">'+i(d)+"</h"+e+">")}),a},p,q=function(a){a+="~0";var b=/^(([ ]{0,3}([*+-]|\d+[.])[ \t]+)[^\r]+?(~0|\n{2,}(?=\S)(?![ \t]*(?:[*+-]|\d+[.])[ \t]+)))/gm;return d?a=a.replace(b,function(a,b,c){var d=b,e=c.search(/[*+-]/g)>-1?"ul":"ol";d=d.replace(/\n{2,}/g,"\n\n\n");var f=p(d);return f=f.replace(/\s+$/,""),f="<"+e+">"+f+"</"+e+">\n",f}):(b=/(\n\n|^\n?)(([ ]{0,3}([*+-]|\d+[.])[ \t]+)[^\r]+?(~0|\n{2,}(?=\S)(?![ \t]*(?:[*+-]|\d+[.])[ \t]+)))/g,a=a.replace(b,function(a,b,c,d){var e=b,f=c,g=d.search(/[*+-]/g)>-1?"ul":"ol",f=f.replace(/\n{2,}/g,"\n\n\n"),h=p(f);return h=e+"<"+g+">\n"+h+"</"+g+">\n",h})),a=a.replace(/~0/,""),a};p=function(a){return d++,a=a.replace(/\n{2,}$/,"\n"),a+="~0",a=a.replace(/(\n)?(^[ \t]*)([*+-]|\d+[.])[ \t]+([^\r]+?(\n{1,2}))(?=\n*(~0|\2([*+-]|\d+[.])[ \t]+))/gm,function(a,b,c,d,e){var f=e,g=b,j=c;return g||f.search(/\n{2,}/)>-1?f=h(E(f)):(f=q(E(f)),f=f.replace(/\n$/,""),f=i(f)),"<li>"+f+"</li>\n"}),a=a.replace(/~0/g,""),d--,a};var r=function(a){return a+="~0",a=a.replace(/(?:\n\n|^)((?:(?:[ ]{4}|\t).*\n+)+)(\n*[ ]{0,3}[^ \t\n]|(?=~0))/g,function(a,b,c){var d=b,e=c;return d=v(E(d)),d=F(d),d=d.replace(/^\n+/g,""),d=d.replace(/\n+$/g,""),d="<pre><code>"+d+"\n</code></pre>",t(d)+e}),a=a.replace(/~0/,""),a},s=function(a){return a+="~0",a=a.replace(/\n```(.*)\n([^`]+)\n```/g,function(a,b,c){var d=b,e=c;return e=v(e),e=F(e),e=e.replace(/^\n+/g,""),e=e.replace(/\n+$/g,""),e="<pre><code class="+d+">"+e+"\n</code></pre>",t(e)}),a=a.replace(/~0/,""),a},t=function(a){return a=a.replace(/(^\n+|\n+$)/g,""),"\n\n~K"+(c.push(a)-1)+"K\n\n"},u=function(a){return a=a.replace(/(^|[^\\])(`+)([^\r]*?[^`])\2(?!`)/gm,function(a,b,c,d,e){var f=d;return f=f.replace(/^([ \t]*)/g,""),f=f.replace(/[ \t]*$/g,""),f=v(f),b+"<code>"+f+"</code>"}),a},v=function(a){return a=a.replace(/&/g,"&amp;"),a=a.replace(/</g,"&lt;"),a=a.replace(/>/g,"&gt;"),a=G(a,"*_{}[]\\",!1),a},w=function(a){return a=a.replace(/(\*\*|__)(?=\S)([^\r]*?\S[*_]*)\1/g,"<strong>$2</strong>"),a=a.replace(/(\*|_)(?=\S)([^\r]*?\S)\1/g,"<em>$2</em>"),a},x=function(a){return a=a.replace(/((^[ \t]*>[ \t]?.+\n(.+\n)*\n*)+)/gm,function(a,b){var c=b;return c=c.replace(/^[ \t]*>[ \t]?/gm,"~0"),c=c.replace(/~0/g,""),c=c.replace(/^[ \t]+$/gm,""),c=h(c),c=c.replace(/(^|\n)/g,"$1  "),c=c.replace(/(\s*<pre>[^\r]+?<\/pre>)/gm,function(a,b){var c=b;return c=c.replace(/^  /mg,"~0"),c=c.replace(/~0/g,""),c}),t("<blockquote>\n"+c+"\n</blockquote>")}),a},y=function(a){a=a.replace(/^\n+/g,""),a=a.replace(/\n+$/g,"");var b=a.split(/\n{2,}/g),d=new Array,e=b.length;for(var f=0;f<e;f++){var g=b[f];g.search(/~K(\d+)K/g)>=0?d.push(g):g.search(/\S/)>=0&&(g=i(g),g=g.replace(/^([ \t]*)/g,"<p>"),g+="</p>",d.push(g))}e=d.length;for(var f=0;f<e;f++)while(d[f].search(/~K(\d+)K/)>=0){var h=c[RegExp.$1];h=h.replace(/\$/g,"$$$$"),d[f]=d[f].replace(/~K\d+K/,h)}return d.join("\n\n")},z=function(a){return a=a.replace(/&(?!#?[xX]?(?:[0-9a-fA-F]+|\w+);)/g,"&amp;"),a=a.replace(/<(?![a-z\/?\$!])/gi,"&lt;"),a},A=function(a){return a=a.replace(/\\(\\)/g,H),a=a.replace(/\\([`*_{}\[\]()>#+-.!])/g,H),a},B=function(a){return a=a.replace(/<((https?|ftp|dict):[^'">\s]+)>/gi,'<a href="$1">$1</a>'),a=a.replace(/<(?:mailto:)?([-.\w]+\@[-a-z0-9]+(\.[-a-z0-9]+)*\.[a-z]+)>/gi,function(a,b){return C(D(b))}),a},C=function(a){function b(a){var b="0123456789ABCDEF",c=a.charCodeAt(0);return b.charAt(c>>4)+b.charAt(c&15)}var c=[function(a){return"&#"+a.charCodeAt(0)+";"},function(a){return"&#x"+b(a)+";"},function(a){return a}];return a="mailto:"+a,a=a.replace(/./g,function(a){if(a=="@")a=c[Math.floor(Math.random()*2)](a);else if(a!=":"){var b=Math.random();a=b>.9?c[2](a):b>.45?c[1](a):c[0](a)}return a}),a='<a href="'+a+'">'+a+"</a>",a=a.replace(/">.+:/g,'">'),a},D=function(a){return a=a.replace(/~E(\d+)E/g,function(a,b){var c=parseInt(b);return String.fromCharCode(c)}),a},E=function(a){return a=a.replace(/^(\t|[ ]{1,4})/gm,"~0"),a=a.replace(/~0/g,""),a},F=function(a){return a=a.replace(/\t(?=\t)/g,"    "),a=a.replace(/\t/g,"~A~B"),a=a.replace(/~B(.+?)~A/g,function(a,b,c){var d=b,e=4-d.length%4;for(var f=0;f<e;f++)d+=" ";return d}),a=a.replace(/~A/g,"    "),a=a.replace(/~B/g,""),a},G=function(a,b,c){var d="(["+b.replace(/([\[\]\\])/g,"\\$1")+"])";c&&(d="\\\\"+d);var e=new RegExp(d,"g");return a=a.replace(e,H),a},H=function(a,b){var c=b.charCodeAt(0);return"~E"+c+"E"}},typeof exports!="undefined"&&(exports=Showdown);

var editing = false;
var drawerIsOpen = false;
var site_content = {};
var page_content = {};
var converter = new Showdown.converter();

function pushToMain(){
  getCredential(function(credential){
    $.ajax({
      url: siteboxhost+"/push",
      type: "POST",
      dataType: "json",
      data: JSON.stringify({
        hash     : window.location.hash,
        location : window.location.href,
        credential:credential
      }),
      contentType: "application/json; charset=utf-8",
      success:function(data, textStatus, jqXHR){
        $.pnotify({'title':'Success','text':'Push Complete - <a href="'+ window.location.href.split('#')[0] +'">View</a>',type:'success'});
      },
      error:function(jqXHR, textStatus, errorThrown){
        $.pnotify({'title':'Success','text':'Push Complete',type:'error'});
      }
    });
  },function(){
    // error
  });
}

function saveNotice(){
  saveAll();
}

function logout(){
  localStorage.removeItem("sitebox-credentials");
  drawerClose();
  editing=false;
}

var credentialUrl = siteboxhost + "/cred"
function getCredential( onSuccess, onFailure ){
  // get credentials from store or login
  credential = localStorage.getItem("sitebox-credentials");
  if(credential) {
    onSuccess(credential)
  } else {
    onFailure()
  }
}

function ajaxLogin(){
  var email = $("#sitebox-login-email").val();
  var paswd = $("#sitebox-login-pass").val();
  
  $.ajax({
    url: credentialUrl,
    type: "POST",
    dataType: "json",
    data: JSON.stringify({
      email : email,
      paswd : paswd
    }),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      var response = data["response"];
      if(response=="ok"){
        $("#sitebox-login-form").remove();
        $("#editor-frame").append(newEditor());
        drawerIsOpen = true;
        var credential = data["credential"];
        localStorage.setItem("sitebox-credentials",credential);
      } else {
        alert("Login Fail");
      }
      
    }
  });
}

function newLogin(){
  var form  = $("<form id='sitebox-login-form'>");
  var title = $("<h3>Please Login to Edit</h3>");
  var hr    = $("<hr/>");
  var emlab = $("<label>Email</label>");
  var email = $("<input type='text' id='sitebox-login-email'/><br/>");
  var pwlab = $("<label>Password</label>");
  var paswd = $("<input type='password' id='sitebox-login-pass'/><br/>");
  var button = $("<input type='button' id='sitebox-login-button' onclick='ajaxLogin();' value='Login'/><br/>");
  paswd.keyup(function(event){
      if(event.keyCode == 13){
          $("#sitebox-login-button").click();
          $("#sitebox-login-email").select();
      }
  });
  form.append(title,hr,emlab,email,pwlab,paswd,button);
  return form;
}

var converters = {
  "DIV":function(item,content){item.html(converter.makeHtml(content));},
  "STYLE":function(item,content){item.text(content);}
}

function newEditor(){
  
  var div = $("<div id='editor'>");
  div.append($("<button id='editor-logout-button' onclick='logout();'>Log Out</button>"));
  
  div.append($("<h2>Page Content</h2>"));
  
  $(".pagebox").each(function(index,item){
    var key = $(item).attr('id');
    div.append( $("<h3>").text(key), 
                $("<textarea id='"+key+"-editor'>").text(page_content[key]).keyup(function(event){
                  var value = this.value;
                  converters[item.nodeName]($("#"+key),value);
                  page_content[key] = value;
                }));
  });
  
  div.append($("<h2>Site Wide Content</h2>"));
  
  $(".sitebox").each(function(index,item){
    var key = $(item).attr('id');
    div.append( $("<h3>").text(key), 
                $("<textarea id='"+key+"-editor'>").text(site_content[key]).keyup(function(event){
                  var value = this.value;
                  $("#"+key).html(converter.makeHtml(value));
                  site_content[key] = value;
                }));
  });
  
  return $("<div id='editor-frame'>").append(div);
}

function saveAll(){
  var credential = localStorage.getItem("sitebox-credentials");
  
  var path = window.location.href.split('#')[0]
  
  var request = {
    credential:credential,
    location:window.location.href,
    page_content:page_content,
    site_content:site_content
  }
  
  var percent = 0;
  var notice = $.pnotify({
    title: "Saving...",
    type: 'info',
    icon: 'picon picon-throbber',
    hide: false,
    closer: false,
    sticker: false,
    opacity: .75,
    shadow: false,
  });
  
  var success = {};
  success.title = "Success!";
  success.type = "success";
  success.hide = true;
  success.closer = true;
  success.sticker = true;
  success.icon = 'picon picon-task-complete';
  success.opacity = 1;
  success.shadow = true;
  
  var failure = {};
  failure.title = "Error";
  failure.text  = "Your updates could not be saved";
  failure.type = "error";
  failure.hide = false;
  failure.closer = true;
  failure.sticker = true;
  failure.icon = 'picon picon-task-complete';
  failure.opacity = 1;
  failure.shadow = true;
  
  log("Saving Updated Contents...")
  $.ajax({
    url:  updateurl,
    type: "POST",
    dataType: "json",
    data: JSON.stringify(request),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Contents Saved");
      window.location.hash = data["hashbang"];
      
      success.text  = "<p>Your changes has been saved but will not \
                       appear as the main version at:\
                       <ul><li><a href='"+ path +"'>"+ path +"</a></li></ul>\
                       You must <button onclick='pushToMain()'>Push Changes</button></p> \
                       <p style='word-wrap:break-word;'>The current version is accessible any time at:\
                       <ul><li style='word-wrap:break-word;'><a href='"+ window.location.href +"'>"+ window.location.href +"</a></li></ul></p> \
                       <div style='margin-top:5px; text-align:right;'> \
                       </div>";
      
      notice.pnotify(success);
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("Not Saved");
      notice.pnotify(failure);
    }
  });
}

function drawerClose(){
  $("body").removeClass("editing");
  $("#windowshade").remove();
  $(".editable").removeClass("editable");
  $("body").animate({
    left: '0'
  }, 500, function() {
    $("#editor-frame").remove();
  });
  $(".edit-label").remove();
  editing = false;
  drawerIsOpen = false;
}

function drawerOpen(){  
  $("body").append("<div id='windowshade' style='opacity:0' onclick='toggleEdit();'>&nbsp;</div>");
  $(".sitebox,.pagebox").each(function(index,item){
    $(item).css("position", "relative");
    $(item).append($("<div class='edit-label'>").text( $(item).attr('id') ));
  }).addClass("editable");
  $("#windowshade").animate({
    opacity: '1.0'
  }, 100, function(){
    // Window Shade Animation complete.
  });
  $("body").animate({
    left: '500px'
  }, 500, function() {
    // Body Animation complete.
  });
  drawerIsOpen = true;
}

function toggleEdit(){
  if(drawerIsOpen){
    if(editing){
      saveNotice();
    }
    drawerClose();
  }else{
    $("body").addClass("editing");
    var frame = $("<div id='editor-frame'>");
    getCredential(function(credential){
      $("html").append( frame.append(newEditor()) );
      editing = true;
      drawerOpen();
    },function(){
      $("html").append( frame.append(newLogin()) );
      drawerOpen();
      $("#sitebox-login-email").select();
    });
  }
}

function log(message){
  $("#console").append($("<p>").text(message));
}

$(function(){
  
  var hashbang= window.location.hash;
  var pagekey = window.location.href;
  var request = {
    location: pagekey,
    version : "current",
    content : "page",
    hash    : hashbang
  };
  
  log("Loading Page Content for: " + pagekey);
  
  $.ajax({
    url:  posturl,
    type: "POST",
    dataType: "json",
    data: JSON.stringify(request),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Json Response Containing Content Received")
      
      for(key in data){
        log("Inserting Content into Element: "+key)
        value = data[key];
        var item = $("#"+key);
        converters[item[0].nodeName](item,value);
      }
      
      log("Content Loaded");
      
      page_content = data;
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("There was an Error: " + textStatus);
    }
  });

  log("Loading Site-Wide Content");
  $.ajax({
    url:  posturl,
    type: "POST",
    dataType: "json",
    data: JSON.stringify({
      version :"current",
      location:window.location.href,
      content :"site",
      hash    : hashbang
    }),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Json Response Containing Site-Wide Content Received")
      
      for(key in data){
        log("Inserting Site-Wide Content into Element: "+key)
        value = data[key];
        var item = $("#"+key);
        converters[item[0].nodeName](item,value);
      }
      
      log("Site-Wide Content Loaded");
      
      site_content = data;
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("There was an Error: " + textStatus);
    }
  });
  
});

$(document).keyup(function(e) {
  var code = (e.keyCode ? e.keyCode : e.which);
  if( code == 27 ){
      toggleEdit();
  }
});

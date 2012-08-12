var siteboxhost = "http://sofiajs.com"
var auth_window;

var handlers = {
  "echo":function(m){
    ipc("alert",m);
  },
  "update":function(m){
    update(m);
  },
  "login":function(_){
    login(function(username){
      ipc("login ok",username);
    },function(_){
      ipc("no login",null);
    })
  },
  "logout":function(_){
    $.ajax({url:siteboxhost+"/logout"});
  },
  "authenticate":function(_){
    oAuth();
  },
  "oauth":function(code){
    authenticate(code);
  }
}

window.onmessage = function(event){
  var data = event.data;
  console.log(data);
  if(handlers[data.type]){
    handlers[data.type](data.message);
  }else{
    console.log("Unexpected Message" + data);
  }
}

function ipc(type,message){
  window.parent.postMessage({
    type   :type,
    message:message
  },"*");
}

var failure = {
  title: "Failure",
  type: 'error',
}

var success = {
  title: "Success",
  type: 'success',
}

function update(request){
  var updateurl = "http://sofiajs.com/update";
  
  $.ajax({
    url:  updateurl,
    type: "POST",
    dataType: "json",
    data: JSON.stringify(request),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      ipc("notice",success);
    },
    error:function(jqXHR, textStatus, errorThrown){
      ipc("notice",failure);
    }
  });
}

function login(success,error){
  $.ajax({
    url:siteboxhost + "/login",
    success:function(data, textStatus, jqXHR){
      success(data);
    },
    error:function(jqXHR, textStatus, errorThrown){
      error(textStatus);
    }
  })
}

function oAuth(){
  url = "https://github.com/login/oauth/authorize?client_id=ec46f5e732b30cc3caca";
  auth_window = window.open(url,'Github','height=400,width=400');
}

function authenticate(code){
  console.log("Authenciate");
  
  $.ajax({
    url: siteboxhost + "/oauth2cred",
    type: "POST",
    dataType: "json",
    data: JSON.stringify({
      code : code
    }),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      ipc("login ok",data.username);
      auth_window.close();
    },
    error:function(x,y,z){
      alert("Unexpected Login Error");
      auth_window.close();
    }
  });
  
}


var siteboxhost = "http://api.sofiajs.com"
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
  "oauth":function(code){
    authenticate(code);
  }
}

window.onmessage = function(event){
  var data = event.data;
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
  var updateurl = "http://api.sofiajs.com/update";
  
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

function authenticate(code){
  console.log("Authenciate");
  
  $.ajax({
    url: siteboxhost + "/authorize",
    type: "POST",
    dataType: "json",
    data: JSON.stringify({
      code : code
    }),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      ipc("login ok",data.username);
    },
    error:function(x,y,z){
      alert("Unexpected Login Error");
    }
  });
  
}


function log(message){
  $("#console").append($("<p>").text(message));
}

$(function(){
  
  var pagekey = window.location.href;
  var request = {
    location: pagekey,
    version : "0.0.0-alpha",
  };
  
  log("Loading content for page: " + pagekey);
  
  $.ajax({
    url:  "/content",
    type: "POST",
    dataType: "json",
    data: JSON.stringify(request),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Json Response Containing Content Received")
      
      for(key in data){
        log("Inserting Content into Element: "+key)
        value = data[key];
        $(key).html(value);
      }
      log("Content Loaded");
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("There was an Error: " + textStatus);
    }
  });
  
});

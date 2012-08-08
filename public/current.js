$.ajax({
  url: "/assets/default.json",
  dataType: "json",
  success:function(data, textStatus, jqXHR){
    for(key in data){
      value = data[key];
      $(key).html(value);
    }
    $("#console").html("Loading content for page: " + window.location.href);
  }
});

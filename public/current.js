var editing = false;
var site_content = {};
var page_content = {};
var converter = new Showdown.converter();

function newEditor(){
  var div = $("<div id='editor'>");
  
  div.append($("<h2>Page Content</h2>"));
  
  for(key in page_content){
    (function(id){
    div.append( $("<h3>").text(key), 
                $("<textarea id='"+key+"-editor'>").text(page_content[key]).keyup(function(event){
                  var value = this.value;
                  $("#"+id).html(converter.makeHtml(value));
                  page_content[id] = value;
                }));
                })(key);
  }
  
  div.append($("<h2>Site Wide Content</h2>"));
  
  for(key in site_content){
    (function(id){
    div.append( $("<h3>").text(key), 
                $("<textarea id='"+key+"-editor'>").text(site_content[key]).keyup(function(event){
                  var value = this.value;
                  $("#"+id).html(converter.makeHtml(value));
                  site_content[id] = value;
                }));
                })(key);
  }
  
  return div;
}

function saveAll(){
  var request = {
    location:window.location.href,
    page_content:page_content,
    site_content:site_content
  }
  
  log("Saving Updated Contents...")
  $.ajax({
    url:  "/content",
    type: "POST",
    dataType: "json",
    data: JSON.stringify(request),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Contents Saved");
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("Not Saved");
    }
  });
  
  
}

function toggleEdit(){
  if(editing){
    $("body").removeClass("editing");
    $("#windowshade").remove();
    $(".editable").removeClass("editable");
    $("body").animate({
      left: '0'
    }, 500, function() {
      $("#editor").remove();
    });
    $(".edit-label").remove();
    saveAll();
    editing = false;
  }else{
    $("body").addClass("editing");
    $("html").append( newEditor() );
    $("body").append("<div id='windowshade' style='opacity:0' onclick='toggleEdit();'>&nbsp;</div>");
    $(".sitebox").each(function(index,item){
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
    editing = true;
  }
}

function log(message){
  $("#console").append($("<p>").text(message));
}

$(function(){
  
  var pagekey = window.location.href;
  var request = {
    location: pagekey,
    version : "0.0.0-alpha",
  };
  
  log("Loading Page Content for: " + pagekey);
  
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
        $("#"+key).html(converter.makeHtml(value));
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
    url:  "/content",
    type: "POST",
    dataType: "json",
    data: JSON.stringify({version:"0.0.0-alpha",location:"http://localhost/site"}),
    contentType: "application/json; charset=utf-8",
    success:function(data, textStatus, jqXHR){
      log("Json Response Containing Site-Wide Content Received")
      
      for(key in data){
        log("Inserting Site-Wide Content into Element: "+key)
        value = data[key];
        $("#"+key).html(converter.makeHtml(value));
      }
      
      log("Site-Wide Content Loaded");
      
      site_content = data;
    },
    error:function(jqXHR, textStatus, errorThrown){
      log("There was an Error: " + textStatus);
    }
  });
  
  $("body").append($("<div id='esc-to-edit'>&nbsp;</div>"));
  
});

$(document).keyup(function(e) {
  var code = (e.keyCode ? e.keyCode : e.which);
  if( code == 27 ){
      toggleEdit();
  }
});

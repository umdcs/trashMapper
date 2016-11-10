var express = require('express');
var bodyParser = require('body-parser');
var app = express()
    
    app.set("port", 4321);

app.use(bodyParser.urlencoded({limit: '50mb', extended: true}));

app.use(bodyParser.json({limit: '50mb'})); 

var json = {};

app.get('/userData', function(req, res) 
	{
	    console.log('/userData GET URI accessed');
	    res.send(json);
	});

app.post('/userData', function (req, res) 
	 {
	     if (!req.body) return res.sendStatus(400)
	     var tempjson = req.body;
	     var picture = tempjson["picture"];
	     var fw = tempjson["foodwaste"];
	     var rec = tempjson["recyclable"];
	     var nrec = tempjson["nrecyclable"];
	     var desc = tempjson["description"];
    	     json[picture] = {};
	     json[fw] = {};
	     json[rec] = {};
	     json[nrec] = {};
	     json[desc] = {};
	     json[picture]["picture"] = tempjson["picture"];
	     json[fw]["foodwaste"] = tempjson["foodwaste"];
	     json[rec]["recyclable"] = tempjson["recyclable"];
	     json[nrec]["nrecyclable"] = tempjson["nrecyclable"];
	     json[desc]["description"] = tempjson["description"];
	     console.log('/userData POST URI accessed');
	     res.json(req.body);
	 })

    app.get('/', function(req, res) 
	    {
		res.send('<HTML><HEAD></HEAD><BODY><H1>hello world</H1></BODY></HTML>');
		console.log(req);
	    });

app.use(function(req, res, next) 
	{
	    res.status(404).send('Sorry cant find that!');
	});

app.use(function(err, req, res, next) 
	{
	    console.error(err.stack);
	    res.status(500).send('Internal Server Error message - very strange request came in and we do not know how to handle it!!!');
	});

app.listen(app.get("port"), function () 
	   {
	       console.log('CS4531 Node Example: Node app listening on port: ', app.get("port"));
	   });

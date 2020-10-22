from flask import Flask
from codeGenerator2 import generateContract
from flask import request
from flask_cors import CORS, cross_origin
app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'


@app.route('/')
def index():
    return "Server is Running"


@app.route('/createcontracts', methods=["GET", "POST"])
def createContracts():
    if request.method == "POST":
        data = (request.get_json(force=True))
        info = {
            "name": data["name"],
            "role": data["role"],
            "tracking": data["tracking"],
            "selfDestruct": True
        }
        contents = generateContract(info)
        contract = ""
        for i in contents:
            contract += i
        return(contract)

    else:
        return "NO GET REQUEST ALLOWED"


if __name__ == '__main__':
    app.run(port=5000, debug=True)

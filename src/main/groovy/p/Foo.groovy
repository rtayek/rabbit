package p
import groovy.util.slurpersupport.*
static void print(File inputFile) {
    if(!inputFile.exists())
        println "$inputFile does not exist!"
    else {
        XmlSlurper parser=new XmlSlurper()
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        GPathResult gPathResult=parser.parse(inputFile)
        gPathResult.childNodes().each {  Node childNode->
            ///println 'child nodes: '+childNode
            //childNode.children().each { println it.name()+': '+it.text() }
            String s="";
            childNode.children().each {
                switch(it.name()) {
                    case 'date':
                        break;
                    case 'millis':
                        break;
                    case 'sequence':
                        s+=it.text()+'|'
                        break;
                    case 'logger':
                        break;
                    case 'level':
                        s+=it.text()+'|'
                        break;
                    case 'class':
                        s+=it.text()+'|'
                        break;
                    case 'method':
                        s+=it.text()+'|'
                        break;
                    case 'thread':
                        s+=it.text()+'|'
                        break;
                    case 'message':
                        s+=it.text()+'|'
                        break;
                    default:
                        println "$it.name() was nod handled! &&&&&&&&&&&&&&&&&&&&&&&&"
                        break;
                }
            }
            println s
        }
    }
}
final File inputFile=new File('a.log')
print(inputFile)

/* VERSION: 0.1.0
 * getting to recognize front
 * RUNNING PATH
 * 1. Calibration. Hold hand flat for this portion. Once the LED begins to blink, it has moved to step 2.
 * 2. Test rotation. If the program believe you are tilting forward, than it will stay on. else, it will blink
 * 3. nothing
 * 
 * 
 */

 #include <Wire.h>

 //CONSTANTS
 const int LEDFRONT = 9;
 const int LEDRIGHT = 12;
 const int LEDBACK = 11;
 const int LEDLEFT = 10;
 const int LOWERRANGE = -25;
 const int UPPERRANGE = 25;

 /* Directions  */

 const int FLAT = 0;
 const int FRONT = 1;
 const int BACK = 2;
 const int RIGHT = 3;
 const int LEFT = 4;
 /* END  */


//globals
long gyroX, gyroY, gyroZ;
float rotX, rotY, rotZ;

int currentState;

 /* This function runs once before the loop begins. This should set the stage for the loop function.*/
void setup() {
  Wire.begin();
  Serial.begin(9600);
  //set up the singal LEDs
   pinMode(LEDFRONT, OUTPUT);
   pinMode(LEDRIGHT, OUTPUT);
   pinMode(LEDBACK, OUTPUT);
   pinMode(LEDLEFT, OUTPUT);

   //LED TEST
   digitalWrite(LEDFRONT, HIGH);
   delay(200);
   digitalWrite(LEDFRONT, LOW);
   
   digitalWrite(LEDLEFT, HIGH);
   delay(200);
   digitalWrite(LEDLEFT, LOW);
   
   digitalWrite(LEDBACK, HIGH);
   delay(200);
   digitalWrite(LEDBACK, LOW);
   
   digitalWrite(LEDRIGHT, HIGH);
   delay(200);
   digitalWrite(LEDRIGHT, LOW);
   //END LED TEST

  //Set up the serial output for testing
  
  Serial.print("END SETUP");
  currentState = FLAT;
}

void loop() {
  Serial.print("LOOP");
  calibration();
  /* if in FLAT state */
  switch(currentState)
  {
        // if flat, we can move in all directions
        case FLAT:
        
                // choose the direction with the highest/lowest direction
                /* This checks to see if FRONT ( large positive X)
                
                 This is to protect against if Y or X is negative */
                if( rotX > abs(rotY) && (rotX > UPPERRANGE))
                {
                    digitalWrite(LEDRIGHT, HIGH);   // turn the LED on (HIGH is the voltage level)
                    delay(100);
                    digitalWrite(LEDRIGHT, LOW);    // turn the LED off by making the voltage LOW
                    currentState = RIGHT; //FRONT
                }
                
                /* This checks to see if RIGHT ( large positive Y)
              
                 This is to protect against if Y or X is negative */
                else if (rotY > abs(rotX) && (rotY > UPPERRANGE))
                {
                      digitalWrite(LEDFRONT, HIGH);   // turn the LED on (HIGH is the voltage level)
                      delay(100);
                     digitalWrite(LEDFRONT, LOW);    // turn the LED off by making the voltage LOW
                      currentState = FRONT; //RIGHT
                }
                /* This checks to see if BACK ( large negative X)
               
                 This is to protect against if Y or X is negative */
                else if ((abs(rotX) > abs(rotY)) && (rotX < LOWERRANGE))
                {
                      digitalWrite(LEDLEFT, HIGH);   // turn the LED on (HIGH is the voltage level)
                      delay(100);
                      digitalWrite(LEDLEFT, LOW);    // turn the LED off by making the voltage LOW
                      currentState = LEFT; // BACK
                }

                /* This checks to see if BACK ( large negative X)
                 1: X is less than Y
                 2: -X is greater than Y
                 This is to protect against if Y or X is negative */
                else if((abs(rotY) > abs(rotX)) && (rotY < LOWERRANGE))
                {
                        digitalWrite(LEDBACK, HIGH);   // turn the LED on (HIGH is the voltage level)
                        delay(100);
                        digitalWrite(LEDBACK, LOW);    // turn the LED off by making the voltage LOW
                        currentState = BACK; //LEFT
                }

                else //else keep with flat
                {
                  currentState = FLAT;
                }
        break;

        case FRONT:
             // check if the current rotation dropped bellow the FLAT range
             //checkLower(rotX, UPPERRANGE);
             if(rotY < LOWERRANGE)
             {
                  currentState = FLAT;
                  digitalWrite(LEDFRONT, LOW);
             }
             
        break;

        case BACK:
            // check if the current rotation rises above the FLAT range
            if(rotY > UPPERRANGE)
            {
              currentState = FLAT;
              digitalWrite(LEDBACK, LOW);
            }
        break;

        case LEFT:
            // left
            if(rotX > UPPERRANGE)
            {
              currentState = FLAT;
              digitalWrite(LEDLEFT, LOW);
            }
        break;

        case RIGHT:
            // right
            if(rotX < LOWERRANGE)
            {
              currentState = FLAT;
              digitalWrite(LEDRIGHT, LOW);
            }
            
        break;
  
  }
  Serial.println(currentState);
  printData();
  delay(500);              // wait for a 100 ms
}

void calibration()
{
  gyroX = analogRead(A0);
  gyroY = analogRead(A1);
}

void processGyroData() 
{
  rotX = gyroX / 131.0;
  rotY = gyroY / 131.0; 
  rotZ = gyroZ / 131.0;
}

/* see if time to move back to center from FRONT or BACK state*/
void checkLower(int rot, int range)
{
  if(rot < range)
  {
    currentState = FLAT; // return to the flat state
  }
}

/* see if time to move back to center from LEFT or RIGHT state*/
void checkUpper(int rot, int range)
{
  if(rot > range)
  {
    currentState = FLAT; // return to the flat state
  }
}

void printData() {
  Serial.print("Gyro (deg)");
  Serial.print(" X=");
  Serial.print(rotX);
  Serial.print(" Y=");
  Serial.print(rotY);
  Serial.print(" Z=");
  Serial.print(rotZ);
  Serial.println();
}



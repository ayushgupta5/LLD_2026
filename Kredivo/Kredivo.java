/*
Referral Service

Schema
// user Info
User
    userID
    userName
    userEmail
    UserPhone
    createdAt
    updatedAt
    total_bonus_amount
    distribution {
        [
            child1:{
                bonus: 50
            },
            child2:{
                bonus: 50
            },
            child3:{
                bonus: 50
            }
        ]

    }

// just a referral Code share by userID and referral code in string format
referral_code
    id (PK)
    userID (FK)
    referralCode
    createdAt
    updatedAt

// relation with parent and child
referral_relation
    id (PK)
    parent_id (FK from User Table)
    child_id (FK from User Table)
    referralCode
    status
    createdAt
    updatedAt

// this is the referral amount with given userID
referral_amount
    id (PK)
    userID (FK from User Table)
    referral_code
    balance_amount
    createdAt
    updatedAt

// make the entries for each referral code redeem
reward_ledger
    id (PK)
    user_id (FK from User Table)
    balance_amount
    reward_type (child bonus (100) | parent bonus (50))
    createdAt
    updatedAt


account
    id (PK)
    parent

1. What children were referred by a particular parent?
        Retrieve the list of children users who were referred by a specific parent user.

        select child_id from referral_relation
        where parent_id = 'given_parent_id'

2. How much bonus amount does a user (child or parent) have overall?
    Calculate the total bonus amount accumulated by a user (child or parent) from all referrals.


        select sum(balance_amount) from reward_ledger
        where user_id='given user id'

        parent --> 50
        child --> 100
        parent2 --> 50
        child2 --> 100


        referralCode Redeem --> make 2 Entries in referral_amount table (1. Parent with 50 and 2. Child with 100)

3. How many children used a particular referral code on a specific day?
    Count the number of children users who used a particular referral code on a given day.

    select count (distinct(child_id)) from referral_relation
    where created_at='given_date' and referral_code = 'given_referral_code'

4. How much bonus amount was given on a particular day?
    Sum up the total bonus amount given to all users (parents and children) on a specific day.

        select sum(balance_amount) from reward_ledger
        where created_at = 'given_date'

5. Which user (child or parent) has received the highest bonus amount to date?
    Identify the user (child or parent) who has accumulated the highest total bonus amount from all referrals.

    select user_id from user
    where balance_amount = (select max(balance_amount) from user)

6. How much bonus amount did a parent receive from each of their children?
    Break down the bonus amount received by a parent user from each of their children users.
        select distribution from user
        where user_id='given_parent_id'

        new Query:-

              list[]=  select referralCode from referral_relation where parent_id='given_parent_id';

                1. select balance_amount from referral_amount where referral_code='given_refferal_code'
                2. select balance_amount from referral_amount where referral_code='given_refferal_code'
                3. select balance_amount from referral_amount where referral_code='given_refferal_code'
                4. select balance_amount from referral_amount where referral_code='given_refferal_code'

                will take the sum of all


*/

